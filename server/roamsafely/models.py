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
from google.appengine.ext import db

class User(db.Model):
  # Personal info.
  name = db.StringProperty(indexed=False)
  address = db.PostalAddressProperty(indexed=False)
  phone_number = db.PhoneNumberProperty(indexed=True)
  
  # Preferences.
  help_unknown_people=db.BooleanProperty(indexed=False)
  send_distress_to_unknown_people=db.BooleanProperty(indexed=False)
  
  # Emergency information
  emergency_phone_1=db.PhoneNumberProperty(indexed=False)
  emergency_phone_2=db.PhoneNumberProperty(indexed=False)
  emergency_phone_3=db.PhoneNumberProperty(indexed=False)
  
  last_known_latitude=db.FloatProperty(indexed=True)
  last_known_longitude=db.FloatProperty(indexed=True)
  last_known_time=db.DateTimeProperty(auto_now_add=True)
  
  is_verified=db.BooleanProperty(indexed=False, default=False)

class SafeLocations(db.Model):
  latitude=db.FloatProperty(indexed=True)
  longitude=db.FloatProperty(indexed=True)
  
class UnsafeLocations(db.Model):
  latitude=db.FloatProperty(indexed=True)
  longitude=db.FloatProperty(indexed=True)