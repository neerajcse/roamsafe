#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import webapp2
import json
from models import *
from twil import *
import logging
from datetime import datetime, timedelta

NOT_FOUND = "The user asked for does not exist"
USER_VERIFIED = "The user has already been verified"
USER_VERIFICATION_SUCCESS="The user was verified successfully"    


def date_handler(obj):
    return obj.isoformat() if hasattr(obj, 'isoformat') else obj

def GetUserWithNumber(user_phone):
    q = User.all()
    q.filter("phone_number =", user_phone)
    results = q.fetch(limit=1)
    return results[0] if len(results)>0 else None

def GetNearestSquires(user, lat, long):
  q = User.all()
  
  # Set the window
  southWestLong = long - 0.1
  northEastLong = long + 0.1
  southWestLat = lat - 0.1
  northEastLat = lat + 0.1
  
  if southWestLong > northEastLong:
    q.filter("last_known_longitude >=" , southWestLong)
    q.filter("last_known_longitude <=" , 180)
    q.filter("last_known_longitude >=" , -180)
    q.filter("last_known_longitude <=", northEastLong)
  else:
    q.filter("last_known_longitude >=", southWestLong)
    q.filter("last_known_longitude <=", northEastLong)

  nearestSquiresAsPerLong = q.fetch(limit=100)
  nearestSquires = [i for i in nearestSquiresAsPerLong if i.last_known_latitude>southWestLat and i.last_known_latitude<northEastLat]
  nearestActiveSquires = [i for i in nearestSquires if i.help_unknown_people]
  return nearestActiveSquires


def SendNotificationToSquiresForUser(nearest_squires, user, lat, long):
  try:
    message = GenerateMessageForPhone(phone=user.phone_number, lat=lat, long=long)
    for squire in nearest_squires:
      SendMessageToPhone(squire.phone_number, message)
  except:
    pass


def UpdateUnsafeLocation(lat, long):
  badLocation = UnsafeLocation()
  badLocation.latitude = float(lat)
  badLocation.longitude = float(long)
  badLocation.put()

def GenerateMessageForPhone(**keywords):
  phone=keywords.get("phone")
  lat=keywords.get("lat")
  long=keywords.get("long")
  custom_location=keywords.get("custom")
  to_friend=keywords.get("to_friend")
  if lat and long:
    return "[URGENT] Distress call from %s. Locate them at http://maps.google.com/?q=%s,%s"% (phone,lat,long)
  else:
    return "[URGENT] Distress call from %s. Locate them at http://maps.google.com/?q=%s"% (phone,custom_location)

def SendMessageToEmergencyContacts(user, message):
  if user.emergency_phone_1:
    SendMessageToPhone(user.emergency_phone_1, message)
  if user.emergency_phone_2:
    SendMessageToPhone(user.emergency_phone_2, message)
  if user.emergency_phone_3:
    SendMessageToPhone(user.emergency_phone_3, message)


def SMSPanicResponseForUser(user, custom_message):
  message = GenerateMessageForPhone(phone=user.phone_number, custom_location=custom_message)
  SendMessageToEmergencyContacts(user, message)


def PanicResponseForUser(user, lat, long):
  # Update unsafe locations
  UpdateUnsafeLocation(lat, long)
  # Get all phone numbers and send message
  message = GenerateMessageForPhone(phone=user.phone_number, lat=lat, long=long)
  SendMessageToEmergencyContacts(user, message)
  # Get all people in a radius of x miles
  nearest_squires = GetNearestSquires(user, lat, long)
  if user.send_distress_to_unknown_people:
    SendNotificationToSquiresForUser(nearest_squires, user, lat, long)
  user.last_panic_time=datetime.now()
  user.put()

    
class NewUserHandler(webapp2.RequestHandler):
  def post(self):
    data = json.loads(self.request.body)
    instance = User(**data)
    if not GetUserWithNumber(instance.phone_number):
      instance.put()
      self.response.write("New user registered successfully.")
    else:
      self.response.write("User already exists")


class UserUpdateHandler(webapp2.RequestHandler):
  def post(self, user_phone):
    user = GetUserWithNumber(user_phone)
    if user:
      update_json = json.loads(self.request.body)
      for property,value in update_json.items():
        setattr(user, property, value)
      user.last_known_time=datetime.now()
      user.put()
      self.response.write(json.dumps(user.__dict__['_entity'], default=date_handler))
    else:
      self.response.write(NOT_FOUND)


class UserGetHandler(webapp2.RequestHandler):
  def get(self, user_phone):
    user = GetUserWithNumber(user_phone)
    if user:
      self.response.write(json.dumps(user.__dict__['_entity'], default=date_handler))
    else:
      self.error(404)


class PanicFromSMSHandler(webapp2.RequestHandler):
  def get(self):
    text = self.request.get("text")
    parts = text.split(",")
    phone_number = parts[0]
    user = GetUserWithNumber(phone_number)
    latitude = None
    longitude = None
    if not user:
      self.response.write("User with phone %s does not exist" % phone_number)
      return
    if len(parts) > 1:
      latitude=parts[1]
    if len(parts) > 2:
      longitude=parts[2]
    if latitude and longitude:
      logging.debug("Sending to normal response as lat long available")
      self.response.write("Normal response sent")
      PanicResponseForUser(user, float(latitude), float(longitude))
    elif latitude:
      logging.debug("sending to sms response")
      self.response.write("SMS response sent")
      SMSPanicResponseForUser(user, latitude)
      

   
class PanicHandler(webapp2.RequestHandler):
  def get(self, user_phone, lat, long):
    user = GetUserWithNumber(user_phone)
    if user:
      PanicResponseForUser(user, float(lat), float(long))
    else:
      self.response.write("User does not exist")


class PanicInfoHandler(webapp2.RequestHandler):
  def get(self, user_phone):
    user = GetUserWithNumber(user_phone)
    if user:
      if datetime.now() - user.last_panic_time > timedelta(minutes=30):
        self.response.write("Bad call")
        return
      else:
        self.response.write("%s,%s,%s" % (user.last_known_latitude, user.last_known_longitude, user.last_known_time))
        return
    self.response.write("Bad call")  
      
class VerificationHandler(webapp2.RequestHandler):
  def get(self, user_phone, verification_code):
    user = GetUserWithNumber(user_phone)
    if user.is_verified:
      self.response.write(USER_VERIFIED)
      return
    else:
      if verification_code == user.verification_code:
        user.is_verified = True
        self.response.write(USER_VERIFICATION_SUCCESS)
        user.put()
        return
      else:
        self.response.write(WRONG_VERIFICATION_CODE)
        return