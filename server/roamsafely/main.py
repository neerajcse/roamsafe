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

def date_handler(obj):
    return obj.isoformat() if hasattr(obj, 'isoformat') else obj

def GetUserWithNumber(user_phone):
    q = User.all()
    q.filter("phone_number =", user_phone)
    results = q.fetch(limit=1)
    return results[0] if len(results)>0 else None

class NewUserHandler(webapp2.RequestHandler):
  def post(self):
    data = json.loads(self.request.body)
    instance = User(**data)
    if GetUserWithNumber(instance.phone_number)
    instance.put()
    self.response.write("Success")

class UserUpdateHandler(webapp2.RequestHandler):
  def post(self, user_phone):
    user = GetUserWithNumber(user_phone)
    if user:
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

class MainHandler(webapp2.RequestHandler):
    def get(self):
        self.response.write('A group of monkeys is working on stuff which will help people stay safe. We will be back soon.')

app = webapp2.WSGIApplication([
    ('/', MainHandler),
    ('/User/PUT', NewUserHandler),
    (r'/User/POST/(\d+)', UserUpdateHandler),
    (r'/User/GET/(\d+)', UserGetHandler),
], debug=True)
