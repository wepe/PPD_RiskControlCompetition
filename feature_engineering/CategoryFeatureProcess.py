import pandas as pd
def merge_train_test(train_file, test_file, output_file):
    train_set = pd.read_csv(train_file, encoding='gb18030')
    print("train_set rows :", train_set.Idx.count())
    test_set = pd.read_csv(test_file, encoding='gb18030')
    print("test_set rows :", test_set.Idx.count())
    train_set = train_set.append(test_set, ignore_index=True)
    train_set.fillna({"target":-1}, inplace=True)
    print("total rows :", train_set.Idx.count())
    train_set.to_csv(output_file, index=False, encoding='utf-8')
def FeatureSplit(feature_type, data_file, categorical_output_file, numerical_output_file):
    data = pd.read_csv(data_file)
    feature_type = pd.read_excel(feature_type)
    feature_type.columns = ['feature', 'type']
    categorical_feature = feature_type[feature_type.type == 'Categorical'].feature.tolist()
    numerical_feature = feature_type[feature_type.type == 'Numerical'].feature.tolist()
    categorical_feature_data = data[["Idx"] + categorical_feature]
    numerical_feature_data = data[["Idx"] + numerical_feature]
    categorical_feature_data.to_csv(categorical_output_file, index=False)
    numerical_feature_data.to_csv(numerical_output_file, index=False)
def merge_update_log_feature(update_feature_file, log_feature_file, output_file):
    update_features = pd.read_csv(update_feature_file)
    log_features = pd.read_csv(log_feature_file)
    features = pd.merge(update_features, log_features, on="Idx", how='outer')
    features.drop(["mostCountInfo", "leastCountInfo", "mostCountCode", "leastCountCode", "mostCountCate", "leastCountCate"], axis=1, inplace=True)
    features.to_csv(output_file, index=False)
def append_update_log_feature(train_feature_file, test_feature_file, output_file):
    train_set = pd.read_csv(train_feature_file)
    print("train_set rows :", train_set.Idx.count())
    test_set = pd.read_csv(test_feature_file)
    print("test_set rows :", test_set.Idx.count())
    train_set = train_set.append(test_set, ignore_index=True)
    print("total rows :", train_set.Idx.count())
    train_set.to_csv(output_file, index=False, encoding='utf-8')
def merge_numberical_features(features_1, features_2, output_file):
    feature_part1 = pd.read_csv(features_1)
    feature_part2 = pd.read_csv(features_2)
    features = pd.merge(feature_part1, feature_part2, on="Idx", how='left')
    features.to_csv(output_file, index=False)
train_file = "E:/mojing/PPD-Second-Round-Data-Update/Training Set/train_master.csv"
test_file = "E:/mojing/PPD-Second-Round-Data-Update/Test Set/Kesci_Master_9w_gbk_1_test_set.csv"
data_file = "E:/mojing/data_all.csv"
feature_type_file = "E:/mojing/feature_type.xlsx"
category_output_file = "E:/mojing/category_feature.csv"
numerical_output_file = "E:/mojing/numerical_feature.csv";
numerical_file = "E:/mojing/numerical_feature_solve.csv"
categorical_encode_file = "E:/mojing/category_encode_feature.csv"
train_update_features_file = "E:/mojing/PPD-Second-Round-Data-Update/Training Set/UpdateInfoFeature_Training_Set.csv"
train_log_features_file = "E:/mojing/PPD-Second-Round-Data-Update/Training Set/LogInfoFeature_Training_Set.csv"
train_merge_file = "E:/mojing/train_log_update_features.csv"
test_update_features_file = "E:/mojing/PPD-Second-Round-Data-Update/Test Set/UpdateInfoFeature_Test_Set.csv"
test_log_features_file = "E:/mojing/PPD-Second-Round-Data-Update/Test Set/LogInfoFeature_Test_Set.csv"
test_merge_file = "E:/mojing/test_log_update_features.csv"
update_log_features = "E:/mojing/update_log_features.csv"
numerical_all_file = "E:/mojing/numerical_feature_all.csv"
merge_numberical_features(numerical_file, update_log_features, numerical_all_file)
# merge_train_test(train_file, test_file, data_file);
# FeatureSplit(feature_type_file, data_file, category_output_file, numerical_output_file)
# merge_update_log_feature(train_update_features_file, train_log_features_file, train_merge_file)
# merge_update_log_feature(test_update_features_file, test_log_features_file, test_merge_file)
# append_update_log_feature(train_merge_file, test_merge_file, update_log_features)
