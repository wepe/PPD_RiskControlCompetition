#coding=utf-8

import pandas as pd

t = pd.read_excel('../data/jw1.xls')
t.columns = ['p','city','w','j']
t.drop('city',axis=1,inplace=True)
t = t.groupby('p').agg('mean')
province = t.to_dict()
j1_province = province['j']
w1_province = province['w']

t = pd.read_csv('../data/jw2.csv',encoding='gbk')
t.columns = ['p','city','w','j']
t.drop('city',axis=1,inplace=True)
t = t.groupby('p').agg('mean')
province = t.to_dict()
j2_province = province['j']
w2_province = province['w']

###
jw1 = pd.read_excel('../data/jw1.xls')
jw1.columns = ['province','city','w','j']
jw1.index = jw1.city
jw1.drop(['province','city'],axis=1,inplace=True)
d1 = jw1.to_dict()
d1_j = d1['j']
d1_w = d1['w']


jw2 = pd.read_csv('../data/jw2.csv',encoding='gbk')
jw2.columns = ['province','city','w','j']
jw2.index = jw2.city
jw2.drop(['province','city'],axis=1,inplace=True)
d2 = jw2.to_dict()
d2_j = d2['j']
d2_w = d2['w']
print d2

def funj(x):
    if x in d1_j:
        return d1_j[x]
    else:
        if x in d2_j:
            return d2_j[x]
        else:
            return None
        

def funw(x):
    if x in d1_w:
        return d1_w[x]
    else:
        if x in d2_w:
            return d2_w[x]
        else:
            return None

def pj(x):
    if x in j1_province:
        return j1_province[x]
    else:
        if x in j2_province:
            return j2_province[x]
        else:
            return None

def pw(x):
    if x in w1_province:
        return w1_province[x]
    else:
        if x in w2_province:
            return w2_province[x]
        else:
            return None

train = pd.read_csv('../data/train/train_master.csv',encoding='gb18030')[['Idx','UserInfo_2','UserInfo_4','UserInfo_8','UserInfo_20','UserInfo_19']]
test = pd.read_csv('../data/test/Kesci_Master_9w_gbk_1_test_set.csv',encoding='gb18030')[['Idx','UserInfo_2','UserInfo_8','UserInfo_20','UserInfo_4','UserInfo_19']]




def func(x):
    if u'市' in x:
        return x[0:-1]
    else:
        return x


####
train['U2_j'] = train.UserInfo_2
train['U2_w'] = train.UserInfo_2
train.drop('UserInfo_2',axis=1,inplace=True)
train.U2_j = train.U2_j.apply(funj)
train.U2_w = train.U2_w.apply(funw)

train['U4_j'] = train.UserInfo_4
train['U4_w'] = train.UserInfo_4
train.drop('UserInfo_4',axis=1,inplace=True)
train.U4_j = train.U4_j.apply(funj)
train.U4_w = train.U4_w.apply(funw)

train['U8_j'] = train.UserInfo_8
train['U8_w'] = train.UserInfo_8
train.drop('UserInfo_8',axis=1,inplace=True)
train.U8_j = train.U8_j.apply(funj)
train.U8_w = train.U8_w.apply(funw)


train['U20_j'] = train.UserInfo_20.apply(func)
train['U20_w'] = train.UserInfo_20.apply(func)
train.drop('UserInfo_20',axis=1,inplace=True)
train.U20_j = train.U20_j.apply(funj)
train.U20_w = train.U20_w.apply(funw)


train['U19_j'] = train.UserInfo_19.apply(func)
train['U19_w'] = train.UserInfo_19.apply(func)
train.drop('UserInfo_19',axis=1,inplace=True)
train.U19_j = train.U19_j.apply(pj)
train.U19_w = train.U19_w.apply(pw)


####

test['U2_j'] = test.UserInfo_2
test['U2_w'] = test.UserInfo_2
test.drop('UserInfo_2',axis=1,inplace=True)
test.U2_j = test.U2_j.apply(funj)
test.U2_w = test.U2_w.apply(funw)

test['U4_j'] = test.UserInfo_4
test['U4_w'] = test.UserInfo_4
test.drop('UserInfo_4',axis=1,inplace=True)
test.U4_j = test.U4_j.apply(funj)
test.U4_w = test.U4_w.apply(funw)

test['U8_j'] = test.UserInfo_8
test['U8_w'] = test.UserInfo_8
test.drop('UserInfo_8',axis=1,inplace=True)
test.U8_j = test.U8_j.apply(funj)
test.U8_w = test.U8_w.apply(funw)

test['U20_j'] = test.UserInfo_20.apply(func)
test['U20_w'] = test.UserInfo_20.apply(func)
test.drop('UserInfo_20',axis=1,inplace=True)
test.U20_j = test.U20_j.apply(funj)
test.U20_w = test.U20_w.apply(funw)


test['U19_j'] = test.UserInfo_19.apply(func)
test['U19_w'] = test.UserInfo_19.apply(func)
test.drop('UserInfo_19',axis=1,inplace=True)
test.U19_j = test.U19_j.apply(pj)
test.U19_w = test.U19_w.apply(pw)


train_test = pd.concat([train,test])
train_test.U2_j.fillna(train_test.U19_j,inplace=True)
train_test.U2_w.fillna(train_test.U19_w,inplace=True)
train_test.U4_j.fillna(train_test.U19_j,inplace=True)
train_test.U4_w.fillna(train_test.U19_w,inplace=True)
train_test.U8_j.fillna(train_test.U19_j,inplace=True)
train_test.U8_w.fillna(train_test.U19_w,inplace=True)
train_test.U20_j.fillna(train_test.U19_j,inplace=True)
train_test.U20_w.fillna(train_test.U19_w,inplace=True)
train_test[['Idx','U2_j','U2_w','U4_j','U4_w','U8_j','U8_w','U20_j','U20_w','U19_j','U19_w']].to_csv('../data/jin_wei.csv',index=None)


