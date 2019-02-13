#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# filename: test_pos.py

import jieba.posseg as pt
#pt.initialize()
pt.pair.__hash__ = lambda self: hash((self.word, self.flag))

from pprint import pprint
from utils import log, json, tqdm
from test_dt import read_json, write_json, read_input, TEST_FILE_1,\
        CUT_SUFFIX, HMM_SUFFIX, NO_HMM_SUFFIX, PY36_EXT, JAVA_EXT

MAX_INF = float("inf")
TEST_FILENAME = "test"
POS_SUFFIX = ".pos"


class PairJSONEncoder(json.JSONEncoder):
    def default(self, obj):
        return {
            "word": obj.word,
            "flag": obj.flag,
        }

class PairJSONDecoder(json.JSONDecoder):
    def __init__(self):
        json.JSONDecoder.__init__(self, object_hook=self.dict2obj)

    @staticmethod
    def dict2obj(d):
        return pt.pair(**d)


sentences = [
    "这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。",
    "我不喜欢日本和服。",
    "雷猴回归人间。",
    "工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作",
    "我需要廉租房",
    "永和服装饰品有限公司",
    "我爱北京天安门",
    "abc",
    "隐马尔可夫",
    "雷猴是个好网站",
    "“Microsoft”一词由“MICROcomputer（微型计算机）”和“SOFTware（软件）”两部分组成",
    "草泥马和欺实马是今年的流行词汇",
    "伊藤洋华堂总府店",
    "中国科学院计算技术研究所",
    "罗密欧与朱丽叶",
    "我购买了道具和服装",
    "PS: 我觉得开源有一个好处，就是能够敦促自己不断改进，避免敞帚自珍",
    "湖北省石首市",
    "湖北省十堰市",
    "总经理完成了这件事情",
    "电脑修好了",
    "做好了这件事情就一了百了了",
    "人们审美的观点是不同的",
    "我们买了一个美的空调",
    "线程初始化时我们要注意",
    "一个分子是由好多原子组织成的",
    "祝你马到功成",
    "他掉进了无底洞里",
    "中国的首都是北京",
    "孙君意",
    "外交部发言人马朝旭",
    "领导人会议和第四届东亚峰会",
    "在过去的这五年",
    "还需要很长的路要走",
    "60周年首都阅兵",
    "你好人们审美的观点是不同的",
    "买水果然后来世博园",
    "买水果然后去世博园",
    "但是后来我才知道你是对的",
    "存在即合理",
    "的的的的的在的的的的就以和和和",
    "I love你，不以为耻，反以为rong",
    "因",
    "",
    "hello你好人们审美的观点是不同的",
    "很好但主要是基于网页形式",
    "hello你好人们审美的观点是不同的",
    "为什么我不能拥有想要的生活",
    "后来我才",
    "此次来中国是为了",
    "使用了它就可以解决一些问题",
    ",使用了它就可以解决一些问题",
    "其实使用了它就可以解决一些问题",
    "好人使用了它就可以解决一些问题",
    "是因为和国家",
    "老年搜索还支持",
    "干脆就把那部蒙人的闲法给废了拉倒！RT @laoshipukong : 27日，全国人大常委会第三次审议侵权责任法草案，删除了有关医疗损害责任“举证倒置”的规定。在医患纠纷中本已处于弱势地位的消费者由此将陷入万劫不复的境地。 ",
    "大",
    "",
    "他说的确实在理",
    "长春市长春节讲话",
    "结婚的和尚未结婚的",
    "结合成分子时",
    "旅游和服务是最好的",
    "这件事情的确是我的错",
    "供大家参考指正",
    "哈尔滨政府公布塌桥原因",
    "我在机场入口处",
    "邢永臣摄影报道",
    "BP神经网络如何训练才能在分类时增加区分度？",
    "南京市长江大桥",
    "应一些使用者的建议，也为了便于利用NiuTrans用于SMT研究",
    "长春市长春药店",
    "邓颖超生前最喜欢的衣服",
    "胡锦涛是热爱世界和平的政治局常委",
    "程序员祝海林和朱会震是在孙健的左面和右面, 范凯在最右面.再往左是李松洪",
    "一次性交多少钱",
    "两块五一套，三块八一斤，四块七一本，五块六一条",
    "小和尚留了一个像大和尚一样的和尚头",
    "我是中华人民共和国公民;我爸爸是共和党党员; 地铁和平门站",
    "张晓梅去人民医院做了个B超然后去买了件T恤",
    "AT&T是一件不错的公司，给你发offer了吗？",
    "C++和c#是什么关系？11+122=133，是吗？PI=3.14159",
    "你认识那个和主席握手的的哥吗？他开一辆黑色的士。",
    "枪杆子中出政权",
    ]


def test_json():
    pt.initialize()
    res = [pt.lcut(s) for s in sentences]
    j = json.dumps(res, cls=PairJSONEncoder)
    print(j)
    k = json.loads(j, cls=PairJSONDecoder)
    print(k)

def get_result(filename, suffix, idx):
    jRess = read_json(filename, suffix, JAVA_EXT, object_hook=PairJSONDecoder.dict2obj)
    print(jRess[idx])


#/////////////////////////////////////////////////////////#

@log
def test_sentences(fn, filename, suffix, *args, **kwargs):
    print("function: %r" % fn.__name__)
    print("args = %s, kwargs = %s" % (args, kwargs))
    res = [fn(s, *args, **kwargs) for s in sentences]
    write_json(res, filename, suffix, cls=PairJSONEncoder)

@log
def analyse_results_sentences(filename, suffix):
    jRess = read_json(filename, suffix, JAVA_EXT, object_hook=PairJSONDecoder.dict2obj)
    pRess = read_json(filename, suffix, PY36_EXT, object_hook=PairJSONDecoder.dict2obj)
    print(" idx │ ljRes │ lpRes │ lAND │ lOR │ lAND/lOR ")
    print(" --- │ ----- │ ----- │ ---- │ --- │ -------- ")
    for idx, (jRes, pRes) in enumerate(zip(jRess, pRess)):
        jSet, pSet = set(jRes), set(pRes)
        s1 = jSet & pSet
        s2 = jSet | pSet
        score = len(s1)/len(s2) if len(s2) != 0 else MAX_INF
        print(" %3d │ %5d │ %5d │ %4d │ %3d │ %.6f" % (
            idx, len(jRes), len(pRes), len(s1), len(s2), score))

def task_test_sentences(fn, filename, suffix, n, *args, **kwargs):
    test_sentences(fn, filename, suffix, *args, **kwargs)
    analyse_results_sentences(filename, suffix)


### test_cut_sentences ###

@log
def task_test_sentences_cut():
    filename = TEST_FILENAME
    suffix   = POS_SUFFIX + CUT_SUFFIX + HMM_SUFFIX
    fn       = pt.lcut
    args     = ()
    kwargs   = {}
    n        = 100
    task_test_sentences(fn, filename, suffix, n, *args, **kwargs)

    #res = get_result(filename, suffix, 10)
    #print(res)

@log
def task_test_sentences_cut_noHMM():
    filename = TEST_FILENAME
    suffix   = POS_SUFFIX + CUT_SUFFIX + NO_HMM_SUFFIX
    fn       = pt.lcut
    args     = ()
    kwargs   = {"HMM": False}
    n        = 100
    task_test_sentences(fn, filename, suffix, n, *args, **kwargs)

    res = get_result(filename, suffix, 10)

### END test_cut_sentences ###


#/////////////////////////////////////////////////////////#

@log
def test_book(fn, filename, suffix, *args, **kwargs):
    print("function: %r" % fn.__name__)
    print("args = %s, kwargs = %s" % (args, kwargs))
    text = read_input(filename)
    segs = fn(text, *args, **kwargs)
    write_json(segs, filename, suffix, cls=PairJSONEncoder)

@log
def analyse_results_book(filename, suffix):
    jRes = read_json(filename, suffix, JAVA_EXT, object_hook=PairJSONDecoder.dict2obj)
    pRes = read_json(filename, suffix, PY36_EXT, object_hook=PairJSONDecoder.dict2obj)
    jSet, pSet = set(jRes), set(pRes)
    s1 = jSet & pSet
    s2 = jSet | pSet
    print("jRes count:", len(jRes))
    print("pRes count:", len(pRes))
    print("jSet & pSet =", len(s1))
    print("jSet | pSet =", len(s2))
    print("score:", len(s1)/len(s2))

@log
def multi_test_book(fn, filename, suffix, n, *args, **kwargs):
    print("function: %r" % fn.__name__)
    print("args = %s, kwargs = %s" % (args, kwargs))
    print("n = %s" % n)
    text = read_input(filename)
    for _ in tqdm(range(n)):
        segs = fn(text, *args, **kwargs)

@log
def task_test_book(fn, filename, suffix, n, *args, **kwargs):
    test_book(fn, filename, suffix, *args, **kwargs)
    analyse_results_book(filename, suffix)
    #multi_test_book(fn, filename, suffix, n, *args, **kwargs)

### test_cut_book ###

@log
def task_test_book_cut():
    #//////////////////////////////////////////#
    # --- TEST_FILE_1 ---
    # jRes count: 151533
    # pRes count: 151472
    # jSet & pSet = 17354
    # jSet | pSet = 17429
    # score: 0.9956968271272018
    #//////////////////////////////////////////#
    # TEST_FILE_1, cost: 16.927970 s
    # TEST_FILE_1, cost: 16.775400 s
    # TEST_FILE_1, n = 20, cost: 274.695603 s
    #//////////////////////////////////////////#
    filename = TEST_FILE_1
    suffix   = POS_SUFFIX + CUT_SUFFIX + HMM_SUFFIX
    fn       = pt.lcut
    args     = ()
    kwargs   = {}
    n        = 20
    task_test_book(fn, filename, suffix, n, *args, **kwargs)

@log
def task_test_book_cut_noHMM():
    #//////////////////////////////////////////#
    # --- TEST_FILE_1 ---
    # jRes count: 156145
    # pRes count: 156148
    # jSet & pSet = 14623
    # jSet | pSet = 14626
    # score: 0.999794885819773
    #//////////////////////////////////////////#
    # TEST_FILE_1, cost: 4.433504 s
    # TEST_FILE_1, cost: 4.062299 s
    # TEST_FILE_1, n = 50, cost: 61.885997 s
    #//////////////////////////////////////////#
    filename = TEST_FILE_1
    suffix   = POS_SUFFIX + CUT_SUFFIX + NO_HMM_SUFFIX
    fn       = pt.lcut
    args     = ()
    kwargs   = {"HMM": False}
    n        = 50
    task_test_book(fn, filename, suffix, n, *args, **kwargs)

### END test_cut_book ###


def test_textrank():
    s = "此外，公司拟对全资子公司吉林欧亚置业有限公司增资4.3亿元，增资后，" + \
        "吉林欧亚置业注册资本由7000万元增加到5亿元。吉林欧亚置业主要经营范围为房地产" + \
        "开发及百货零售等业务。目前在建吉林欧亚城市商业综合体项目。2013年，实现营业" + \
        "收入0万元，实现净利润-139.13万元。"
    import jieba.analyse
    res = jieba.analyse.textrank(s, topK=20, withWeight=True, withFlag=True)
    #print(res)
    a = ['公司', '全资', '子公司', '吉林', '欧亚', '置业', '有限公司', '增资', '注册资本', '增加', '经营范围', '开发', '百货', '零售', '业务', '在建', '城市', '商业', '综合体', '项目', '实现', '营业', '收入', '净利润']
    b = ["实现", "零售", "注册资本", "营业", "置业", "城市", "业务", "欧亚", "开发", "百货", "增资", "收入", "子公司", "吉林", "项目", "全资", "商业", "经营范围", "综合体", "在建", "公司", "净利润", "有限公司"]
    print(set(a)-set(b))
    print(set(b)-set(a))


if __name__ == '__main__':
    pt.initialize()

    res = pt.lcut(sentences[10])
    res = pt.lcut(sentences[10], HMM=False)
    #print(res)

    #task_test_sentences_cut()
    #task_test_sentences_cut_noHMM()

    #task_test_book_cut()
    #task_test_book_cut_noHMM()
    test_textrank()

