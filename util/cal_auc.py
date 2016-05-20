from sklearn.metrics import roc_auc_score
import pandas as pd

val_set = pd.read_csv('E:/mojing/train_val/val.csv')
val_pred = pd.read_csv('E:/mojing/train_val/rf_lsvr.csv')
auc = roc_auc_score(val_set.target.values, val_pred.score.values)
print(auc)
