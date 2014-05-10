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
from models import*

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

def SendMessageToPhone(phone_number):
  pass


def GetNearestSquires(user):
  pass

def SendNotificationToSquiresForUser(nearest_squires, user):
  pass

def PanicResponseForUser(user):
  # Get all phone numbers and send message
  SendMessageToPhone(user.emergency_phone_1)
  SendMessageToPhone(user.emergency_phone_2)
  SendMessageToPhone(user.emergency_phone_3)
  # Get all people in a radius of x miles
  nearest_squires = GetNearestSquires(user)
  SendNotificationToSquiresForUser(nearest_squires, user)
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
  def get(self, user_phone):
    user = GetUserWithNumber(user_phone)
    if user:
      
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
    
      
class MainHandler(webapp2.RequestHandler):
    def get(self):
        self.response.write('A group of monkeys is working on stuff which will help people stay safe. We will be back soon.')

app = webapp2.WSGIApplication([
    ('/', MainHandler),
    ('/User/PUT', NewUserHandler),
    (r'/User/POST/(\d+)', UserUpdateHandler),
    (r'/User/GET/(\d+)', UserGetHandler),
    (r'/User/Verify/(\d+)/(\d+)', VerificationHandler),
    (r'/User/Panic/(\d+)', PanicHandler)
], debug=True)
