#Load the drivers

sudo modprobe w1-gpio
sudo modprobe w1-therm


#Show available devices (example 28-0000055f327d)

ls /sys/bus/w1/devices


#Read the output from the device

cat /sys/bus/w1/devices/28-0000055f327d/w1_slave


#Run python script

sudo /usr/bin/python /home/pi/seniorDesign/tempSense.py


#List current crontab jobs
sudo crontab -l

#edit the list of cronjobs
sudo crontab -e


#note, enter all ON ONE LINE
* * * * * sudo /usr/bin/python /home/pi/seniorDesign/tempSense.py > /dev/null 2>&1
