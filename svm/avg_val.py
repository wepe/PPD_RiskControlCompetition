#coding=utf-8

"""
Author: wepon (http://2hwp.com/)

"""
from sklearn.metrics import roc_auc_score
import pandas as pd 
import os

val = pd.read_csv('../data/validation/validation_set.csv')




"""
for i in range(30):
    xgb = pd.read_csv('./val/svm_{0}.csv'.format(i))
    tmp = pd.merge(xgb,val,on='Idx')
    auc = roc_auc_score(tmp.target.values,tmp.score.values)
    xgb.to_csv('./val/svm{0}_{1}.csv'.format(i,auc),index=None,encoding='utf-8')
"""


files = os.listdir('./val')
pred = pd.read_csv('./val/'+files[0])
Idx = pred.Idx
score = pred.score
for f in files[1:]:
    pred = pd.read_csv('./val/'+f)
    score += pred.score

score /= len(files)
pred = pd.DataFrame(Idx,columns=['Idx'])
pred['score'] = score

tmp = pd.merge(pred,val,on='Idx')


auc = roc_auc_score(tmp.target.values,tmp.score.values)
pred.to_csv('./val/avg_{0}.csv'.format(auc),index=None,encoding='utf-8')
