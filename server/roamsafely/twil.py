from nexmomessage import NexmoMessage

def SendMessageToPhone(phone_number, lat, long):
  m = "[URGENT]Distress call from " + phone_number + ". Locate them at http://maps.google.com/?q=%s,%s"% (lat,long);
  msg = {'reqtype': 'json' , 'api_secret': '8e7c547e', 'from': '12069396660', 'to': phone_number, 'api_key': '4a08f329'}
  # text message
  msg['text'] = m
  sms1 = NexmoMessage(msg)
  print("SMS details: %s") % sms1.get_details()
  sms1.set_text_info(m)
  print("SMS details: %s") % sms1.get_details()
  print sms1.send_request()