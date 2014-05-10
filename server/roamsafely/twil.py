import urllib
import urllib2
import base64
import string

BASE_URL = "https://api.twilio.com/2010-04-01/"
REGISTERED_PHONE = "+12677725665"


def SendMessageToPhone(phone_number):
  """url = BASE_URL + "Accounts/ACbb8d0eac30c4f08ff027f8f552acafb6/Messages/"
  post_data_dictionary = {"from":REGISTERED_PHONE, "to":phone_number, "Body": "Test text message"}
  username = "ACbb8d0eac30c4f08ff027f8f552acafb6"
  password = "5aa823aee026ec321b01a343ab03c853"

  auth = base64.encodestring('%s:%s' % (username, password)).replace('\n', '')
  
  #sets the user agent header
  http_headers = {"User-Agent":"Mozilla/4.0 (compatible; MSIE 5.5;Windows NT)"}
  http_headers["Authorization"] = "Basic %s" % auth

  post_data_encoded = urllib.urlencode(post_data_dictionary)
  request_object = urllib2.Request(url, post_data_encoded, http_headers)

  response = urllib2.urlopen(request_object)
  html_string = response.read()"""
  pass