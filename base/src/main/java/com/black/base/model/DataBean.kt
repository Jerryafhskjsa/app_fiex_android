package com.black.base.model

class DataBean {
    var imageRes: Int? = null
    var imageUrl: String? = null
    var title: String?
    var viewType: Int

    constructor(imageRes: Int?, title: String?, viewType: Int) {
        this.imageRes = imageRes
        this.title = title
        this.viewType = viewType
    }

    constructor(imageUrl: String?, title: String?, viewType: Int) {
        this.imageUrl = imageUrl
        this.title = title
        this.viewType = viewType
    }

    companion object {

        //测试数据，如果图片链接失效请更换
        val testData3: List<DataBean>
            get() {
                val list: MutableList<DataBean> = ArrayList()
                list.add(
                    DataBean(
                        "https://img.zcool.cn/community/013de756fb63036ac7257948747896.jpg",
                        null,
                        1
                    )
                )
                list.add(
                    DataBean(
                        "https://img.zcool.cn/community/01639a56fb62ff6ac725794891960d.jpg",
                        null,
                        1
                    )
                )
                list.add(
                    DataBean(
                        "https://img.zcool.cn/community/01270156fb62fd6ac72579485aa893.jpg",
                        null,
                        1
                    )
                )
                list.add(
                    DataBean(
                        "https://img.zcool.cn/community/01233056fb62fe32f875a9447400e1.jpg",
                        null,
                        1
                    )
                )
                list.add(
                    DataBean(
                        "https://img.zcool.cn/community/016a2256fb63006ac7257948f83349.jpg",
                        null,
                        1
                    )
                )
                return list
            }
    }
}