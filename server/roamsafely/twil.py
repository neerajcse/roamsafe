import urllib
import urllib2

BASE_URL = "https://api.twilio.com/2010-04-01/"

def SendMessageToPhone(phone_number)
  url = BASE_URL + "Accounts/ACbb8d0eac30c4f08ff027f8f552acafb6/Messages"
  post_data_dictionary = {"firstname":"Devin", "lastname":"Cornell"}

  #sets the user agent header
  http_headers = {"User-Agent":"Mozilla/4.0 (compatible; MSIE 5.5;Windows NT)"}

  post_data_encoded = urllib.urlencode(post_data_dictionary)
  request_object = urllib2.Request(url, post_data_encoded, http_headers)

  response = urllib2.urlopen(request_object)
  html_string = response.read()