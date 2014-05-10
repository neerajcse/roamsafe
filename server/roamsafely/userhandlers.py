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
  southWestLong = long - 2
  northEastLong = long + 2
  southWestLat = lat - 2
  northEastLat = lat + 2
  
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
  pass

def UpdateUnsafeLocation(lat, long):
  badLocation = UnsafeLocation()
  badLocation.latitude = float(lat)
  badLocation.longitude = float(long)
  badLocation.put()
  
def PanicResponseForUser(user, lat, long):
  # Update unsafe locations
  UpdateUnsafeLocation(lat, long)
  # Get all phone numbers and send message
  if user.emergency_phone_1:
    SendMessageToPhone(user.emergency_phone_1, lat, long)
  if user.emergency_phone_2
    SendMessageToPhone(user.emergency_phone_2, lat, long)
  if user.emergency_phone_3
    SendMessageToPhone(user.emergency_phone_3, lat, long)
  # Get all people in a radius of x miles
  nearest_squires = GetNearestSquires(user, lat, long)
  if user.send_distress_to_unknown_people:
    SendNotificationToSquiresForUser(nearest_squires, user, lat, long)
  # Send a notification to all those people
    
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
      self.response.write(NOT_FOUND)

class PanicHandler(webapp2.RequestHandler):
  def get(self, user_phone, lat, long):
    user = GetUserWithNumber(user_phone)
    if user:
      PanicResponseForUser(user, float(lat), float(long))
    else:
      self.response.write("User does not exist")
  
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
