import pandas as pd
n = 0;
def read_feature(fname, fdata):
    print("solving ", fname)
    fn = pd.read_csv(fname)
#     fn = fn[fn.score >= 10]
#     fn = fn[fn.score < 10]
#     fn = fn[fn.score >= 7]
#     fn = fn[fn.score < 7]
#     fn = fn[fn.score >= 6]
    global n;
    fn = fn[fn.score == 4]
    n += fn.shape[0]
    fd = pd.read_csv(fdata)[["Idx"] + fn.feature.tolist()]
    return fd;
rate_features_1 = read_feature("E:/mojing/feature_score_1.csv", "E:/mojing/third_part_rate_1.csv")
rate_features_2 = read_feature("E:/mojing/feature_score_2.csv", "E:/mojing/third_part_rate_2.csv")
rate_features_3 = read_feature("E:/mojing/feature_score_3.csv", "E:/mojing/third_part_rate_3.csv")
rate_features_4 = read_feature("E:/mojing/feature_score_4.csv", "E:/mojing/third_part_rate_4.csv")
rate_features_5 = read_feature("E:/mojing/feature_score_5.csv", "E:/mojing/third_part_rate_5.csv")
rate_features_6 = read_feature("E:/mojing/feature_score_6.csv", "E:/mojing/third_part_rate_6.csv")
rate_features_7 = read_feature("E:/mojing/feature_score_7.csv", "E:/mojing/third_part_rate_7.csv")
print("features = ", n)
print("start merge")
features_thirdpart_rate = pd.merge(rate_features_1, rate_features_2, on="Idx", how="left")
print("start merge 3")
features_thirdpart_rate = pd.merge(features_thirdpart_rate, rate_features_3, on="Idx", how="left")
print("start merge 4")
features_thirdpart_rate = pd.merge(features_thirdpart_rate, rate_features_4, on="Idx", how="left")
print("start merge 5")
features_thirdpart_rate = pd.merge(features_thirdpart_rate, rate_features_5, on="Idx", how="left")
print("start merge 6")
features_thirdpart_rate = pd.merge(features_thirdpart_rate, rate_features_6, on="Idx", how="left")
print("start merge 7")
features_thirdpart_rate = pd.merge(features_thirdpart_rate, rate_features_7, on="Idx", how="left")
features_thirdpart_rate.to_csv("E:/mojing/features_thirdpart_rate_part5.csv", index=False)
