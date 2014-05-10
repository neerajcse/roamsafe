from nexmomessage import NexmoMessage
import logging

def SendMessageToPhone(phone_number, message):
  logging.debug("Sending message to phone %s with m=%s" % (phone_number, message))
  msg = {'reqtype': 'json' , 'api_secret': '8e7c547e', 'from': '12069396660', 'to': phone_number, 'api_key': '4a08f329'}
  # text message
  msg['text'] = message
  sms1 = NexmoMessage(msg)
  print("SMS details: %s") % sms1.get_details()
  sms1.set_text_info(message)
  print("SMS details: %s") % sms1.get_details()
  print sms1.send_request()