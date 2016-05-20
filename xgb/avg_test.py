#coding=utf-8

"""
Author: wepon (http://2hwp.com/)

"""
from sklearn import preprocessing
import pandas as pd 
import os


files = os.listdir('./test')
pred = pd.read_csv('./test/'+files[0])
Idx = pred.Idx
score = pred.score
for f in files[1:]:
    pred = pd.read_csv('./test/'+f)
    score += pred.score

score /= len(files)



pred = pd.DataFrame(Idx,columns=['Idx'])
pred['score'] = score
pred.to_csv('./test/avg_preds.csv',index=None,encoding='utf-8')
