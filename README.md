### [拍拍贷风险控制大赛铜奖解决方案](https://github.com/wepe/PPD_RiskControlCompetition)

- 赛题介绍

	请参见 [Kesci官网介绍](http://www.kesci.com/apps/home_log/index.html#!/competition/56cd5f02b89b5bd026cb39c9)

- 参赛队伍

	不得仰视本王

	队员: [天音](https://tianchi.shuju.aliyun.com/science/scientistDetail.htm?spm=5176.100065.0.0.gnyTyY&userId=713068774)，[Cyber](https://tianchi.shuju.aliyun.com/science/scientistDetail.htm?spm=0.0.0.0.hITk7I&userId=728128096)，[Bryan](http://blog.csdn.net/bryan__)，[赵蕊](https://tianchi.shuju.aliyun.com/science/scientistDetail.htm?spm=5176.100170.111.28.PytD5w&userId=312633256)，[wepon](http://2hwp.com/)

- 解决方案

	详细解决方案请看 [PDF文件](https://github.com/wepe/PPD_RiskControlCompetition/blob/master/%E9%A3%8E%E6%8E%A7%E7%AE%97%E6%B3%95%E5%A4%A7%E8%B5%9B%E8%A7%A3%E5%86%B3%E6%96%B9%E6%A1%88.pdf)

- 代码目录说明

	- `proccess  文件夹`
	
		- Split.java将采样数据分块
		- CombineSample.java将采样数据合并
		- NThreadRNB.java和NThreadMatrix.java是并行采样代码工具类
	
	
	- `feature engineering  文件夾`
	
		- NullDiscrete.java和CityFeature.java为计算城市向量特征
		- city_feature.sql 为数据库处理代码
		- rank.py 对原始数值特征进行排序，得到排序特征
		- null.py 分析和处理缺失值
		- jw.py  生成经纬度特征
		- CategoryFeatureProcess.py	特征处理
		- MergeFeature.py	特征合并
		- SelectFeature.py	特征筛选
		- jingch文件夹
			- UserLogInfoFeature	 登录信息特征提取类
			- UserUpdateInfoFeature	修改信息特征提取类
			- MergeTool	模型融合工具类
			- FeatureProcess	特征处理类
	
	
	
	- `feature_select 文件夹`
	
		- sort_feature_using_xgb.py  训练xgb模型对特征进行重要性排序，特征选择
		- avg_featurescore.py 将多份featurescore文件全加，得到特征重要性排序文件
	
	
	
	
	- `sample  文件夹`
	
		包含了两种采样代码，其中MY0为本次比赛使用的基于粗糙集的并行过采样算法
	
	
	- `util  文件夹`
	
		  这个文件夹包含一些常用的工具类和代码
		- visualize_null.py 缺失值的可视化分析
		- visualize_dataset.py 对每日成交量和违约量进行可视化
		- plot_feature_importance.py 画特征重要性图
		- cal_auc.py 计算auc，线下验证
	
	
	- `lr文件夹`
	
		- lr.py  逻辑回归模型文件
	
	
	
	- `svm 文件夹`
	
		- svm.py  运用数据集分解的方法训练多个svm进行averaging
		- avg_val.py  查看在验证集上的效果
		- avg_test.py 查看在测试集上的效果
	
	
	- `xgb文件夹`
	
		- single_xgb.py  单模型xgboost，线下cv的auc值是0.782左右
		- bagging_xgb.py  单模型xgboost的改进版，对参数和特征加入随机扰动，训练多个xgboost子模型
		- avg_test.py  对子模型预测结果进行averaging
	    - graphlab_xgboost.py graphlab版本的xgboost
	
	- `ensemble文件夹`
	
		- cal_mic.py 计算单模型结果文件之间的相关性
		- ensemble.py 多个单模型的加权融合,通过验证集选取最优参数
		- blend_ensemble.py 训练多个单模型，并进行blending融合
	

- 使用须知

	- 代码可以自由使用，但请保留出处。


	
