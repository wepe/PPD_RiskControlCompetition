#coding=utf-8

"""
Author: wepon (http://2hwp.com/)

"""

import numpy as np
import pandas as pd
import matplotlib.pylab as plt


train = pd.read_csv('../data/train/train_x_null.csv')[['Idx','n_null']]
train_y = pd.read_csv('../data/train/train_master.csv',encoding='gb18030')[['Idx','target']]
train = pd.merge(train,train_y,on='Idx')
train = train.sort(columns='n_null')

train_missing_gt130= train[train.n_null>130]
train_missing_gt130.to_csv('../data/train_missing_gt130.csv',index=None)

print len(train_missing_gt130)

test = pd.read_csv('../data/test/test_x_null.csv')[['Idx','n_null']]
test = test.sort(columns='n_null')

t = train.n_null.values
#y = train.target.values
#y1 = [sum(y[0:i+1]) for i in range(len(y))]
x = range(len(t))
plt.scatter(x,t,c='k')
#plt.plot(x,y1,c='b')
plt.title('train set')
plt.xlabel('Order Number(sort increasingly)')
plt.ylabel('Number of Missing Attributes')  
plt.ylim(0,170) 
plt.show()

t = test.n_null.values
x = range(len(t))
plt.scatter(x,t,c='b')
plt.title('test set')
plt.ylim(0,170) 
plt.xlabel('Order Number(sort increasingly)')
plt.ylabel('Number of Missing Attributes')
plt.show()



"""
#按列统计,画直方图
x = [97,97,63,63,63,10,10,6,6,6,6,1,1,1]
index = np.arange(1,len(x)+1)
bar_width = 0.35
opacity = 0.7
plt.bar(index, x, bar_width,alpha=opacity,color='#87CEFA')
plt.xlabel('Attributes')
plt.ylabel('Missing rate(%)')  
plt.title('Missing rate of Attributes')  
plt.xticks(index-0.5 + bar_width, ('WeblogInfo_1', 'WeblogInfo_3', 'UserInfo_11', 'UserInfo_12', 'UserInfo_13','WeblogInfo_19','WeblogInfo_21','WeblogInfo_2','WeblogInfo_4','WeblogInfo_5','WeblogInfo_8','UserInfo_2','UserInfo_4','WeblogInfo_23~49'),rotation=45)  
plt.ylim(0,100)  
plt.show()  

"""
