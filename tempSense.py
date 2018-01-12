import os
import glob
import datetime
import time
 
os.system('modprobe w1-gpio')
os.system('modprobe w1-therm')
 
#Change to your device path(s)
lst = []
lst.append('/sys/bus/w1/devices/28-0417228c77ff/w1_slave')
# lst.append('/sys/bus/w1/devices/28-0000055f327d/w1_slave')
 
#Change to your Log Path
temp_log = '/home/pi/seniorDesign/temp.csv'
date_log = str(datetime.datetime.now())
 
def get_temp(device):
    #To read the sensor data, just open the w1_slave file
    f = open(device, 'r')
    data = f.readlines()
    f.close()
    deg_f = ''
    if data[0].strip()[-3:] == 'YES':
        temp = data[1][data[1].find('t=')+2:]
        #If temp is 0 or not numeric an exception 
        #will occur so lets handle it gracefully
        try:
            if float(temp)==0:
                deg_f = 32
            else:
                deg_f = (float(temp)/1000)*9/5+32
        except:
            print "Error with t=", temp
            pass
    return deg_f
 
for device in lst:
    device_name = device.split('/')[5]
    with open(temp_log, 'a') as f:
        s = device_name + ','
        s += date_log + ','
        s += str(get_temp(device)) + '\r\n'
        print(s)
        f.write(s)
    #When there are multiple devices, a short pause 
    #interval between reading sensors seems to work best
    time.sleep(1)
