# -*- coding: utf-8 -*-
"""
Created on Mon Apr 09 00:24:30 2018

@author: NB VENKATESHWARULU
"""

print "Hello Testing Pyton"
import subprocess
import csv
import json
from firebase import firebase
import pandas as pd
from pandas import Series
# Define command and arguments
command = 'Rscript'
path2script = 'C:/Users/NB VENKATESHWARULU/Desktop/Thesis_Work/Data/AppWork/EnergyApp/upload_firebase.R'

# Variable number of args in a list
args = ["Apartment302","e818b558-5d35-5196-8844-34a25d02a511"]

# Build subprocess command
cmd = [command, path2script]

# check_output will run the command and store to result
x = subprocess.check_output(cmd, universal_newlines=True)

#print('The maximum of the numbers is:', x)
#subprocess.call(["ls", "-l"],shell=TRUE)

print x
print "In python"

def resample(df,rate):
    
    ts=Series(df['power'],index=df.index)
    resampled_data=ts.resample(rate).mean() #mean()
    resampled_data=resampled_data.fillna(0)
    print  "resampled"
    print resampled_data
    return resampled_data


#dynamic_filepath="C:/Users/NB VENKATESHWARULU/Desktop/Thesis_Work/Data/Energy/dynamic/"

#df = pd.read_csv(dynamic_filepath+'Apartment302_dynamic.csv')
reader = csv.DictReader(x.decode('ascii').splitlines(),
                        delimiter=' ', skipinitialspace=True,
                        fieldnames=['index','date','timestamp', 'power',
   
                                 ])
timestamp_values=[]
power_values=[]
next(reader)
for row in reader:
    
    date = row.get('date')
    times= row.get('timestamp')
    #print row,times,date
    timestamp_values.append(date+' '+times)
    power_values.append(float(row.get('power')))


df= pd.DataFrame(
    {'timestamp': timestamp_values,
     'power': power_values
    })

#print df

df['timestamp'] = pd.to_datetime(df['timestamp'],format='%Y-%m-%d %H:%M:%S')


df.index = df['timestamp']
resampled_data=resample(df,'10min')
#print resampled_data
#print resampled_data['timestamp']
resampled_data=pd.Series.to_frame(resampled_data)
print resampled_data.head()
out = resampled_data.to_json(orient='records')

#print out
print resampled_data
firebase = firebase.FirebaseApplication('https://energyapp-7492d.firebaseio.com/', None)
new_data={}
new_data['Apartment201test1']=out

result = firebase.patch('/dynamic_data',new_data)

import pip
with open("requirements.txt", "w") as f:
    for dist in pip.get_installed_distributions():
        req = dist.as_requirement()
        f.write(str(req) + "\n")
