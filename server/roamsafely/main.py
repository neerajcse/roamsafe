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
from userhandlers import *

def GetAllUnsafeLocationsWithin(southWestLat, southWestLong, northEastLat, northEastLong):
  q = UnsafeLocation.all()
  if southWestLong > northEastLong:
    q.filter("longitude >=" , southWestLong)
    q.filter("longitude <=" , 180)
    q.filter("longitude >=" , -180)
    q.filter("longitude <=", northEastLong)
  else:
    q.filter("longitude >=", southWestLong)
    q.filter("longitude <=", northEastLong)

  unsafeLocationsOnLong = q.fetch(limit=100)
  unsafeLocations = [i for i in unsafeLocationsOnLong if i.latitude>southWestLat and i.latitude<northEastLat]
  return unsafeLocations

class UnsafeLocationsHandler(webapp2.RequestHandler):
  def get(self, southWestLat, southWestLong, northEastLat, norttEastLong):
    unsafeLocations = GetAllUnsafeLocationsWithin(float(southWestLat), float(southWestLong), float(northEastLat), float(norttEastLong))
    for location in unsafeLocations:
      self.response.write(str(location.latitude) + "," + str(location.longitude) + ";")
      
class MainHandler(webapp2.RequestHandler):
    def get(self):
        self.response.write('A group of monkeys is working on stuff which will help people stay safe. We will be back soon.')

app = webapp2.WSGIApplication([
    ('/', MainHandler),
    ('/User/PUT', NewUserHandler),
    (r'/User/POST/(\d+)', UserUpdateHandler),
    (r'/User/GET/(\d+)', UserGetHandler),
    (r'/User/Verify/(\d+)/(\d+)', VerificationHandler),
    (r'/User/Panic/(\d+)/([+|-]?\d+)/([+|-]?\d+)', PanicHandler),
    (r'/UnsafeLocations/([+|-]?\d+.?\d+)/([+|-]?\d+.?\d+)/([+|-]?\d+.?\d+)/([+|-]?\d+.?\d+)', UnsafeLocationsHandler)
], debug=True)
