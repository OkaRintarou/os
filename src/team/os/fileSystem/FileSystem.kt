package team.os.fileSystem

import java.io.*
import kotlin.experimental.and
import kotlin.experimental.xor

class FileSystem() : IFileSystem {
    private val disk : File
    private val diskAccessor : RandomAccessFile
    private val diskInfo : DiskInformation
    private val recordBitmap : ByteArray
    private val dataBitmap : ByteArray
    private val fileDescriptorList : ArrayList<FileDescriptor>
    private val maxFileOpen : Int

    init {
        disk = File("disk")
        if (!disk.exists()) {
            disk.createNewFile()
        }
        diskAccessor = RandomAccessFile(disk, "rw")
        diskInfo = loadDiskInfo()
        recordBitmap = loadRecordBitmap()
        dataBitmap = loadDataBitmap()
        maxFileOpen = 100
        fileDescriptorList = ArrayList<FileDescriptor>(0)
        for (i in 0 until maxFileOpen) {
            fileDescriptorList.add(FileDescriptor())
        }
    }

    /**
     * 将硬盘信息保存到文件中
     */
    fun saveDiskInfo() {
        diskAccessor.seek(0) //硬盘元文件保存在硬盘的第一个块中，因此将指针移至硬盘开头
        val diskInfoByteArray : ByteArray = diskInfo.toByteArray() //ByteArray的大小是1024,
        diskAccessor.write(diskInfoByteArray)
    }

    /**
     * 从文件中读取硬盘信息
     * @return 返回信息读取正确的DiskInformation
     */
    private fun loadDiskInfo() : DiskInformation {
        diskAccessor.seek(0) //硬盘元文件保存在硬盘的第一个块中，因此将指针移至硬盘开头
        val diskInfoByteArray : ByteArray = readOneBlock()
        val info : DiskInformation = DiskInformation()
        info.fromByteArray(diskInfoByteArray)
        return info
    }

    /**
     * 从文件中读取RecordBitmap
     * @return 返回保存有Record空闲信息的ByteArray
     */
    private fun loadRecordBitmap() : ByteArray {
        val array : ByteArray = ByteArray(diskInfo.recordBitmapBlocks * diskInfo.blockSize)
        readBlock(array, 1, diskInfo.recordBitmapBlocks)
        return array
    }

    /**
     * 从文件中读取DataBitmap
     * @return 返回保存有Data块空闲信息的ByteArray
     */
    private fun loadDataBitmap() : ByteArray {
        val array : ByteArray = ByteArray(diskInfo.dataBitmapBlocks * diskInfo.blockSize)
        readBlock(array, 1 + diskInfo.recordBitmapBlocks, diskInfo.dataBitmapBlocks)
        return array
    }

    /**
     * 从硬盘中读出一个物理块，读写指针需要事先设置
     * @return 返回一个长度为1024字节（块大小）的ByteArray
<<<<<<< HEAD
=======
     * 显式表示1024的原因是初始化之前调用时不知道diskInfo.blockSize
>>>>>>> origin/master
     */
    private fun readOneBlock() : ByteArray {
        val array : ByteArray = ByteArray(1024)
        diskAccessor.read(array, 0, 1024)
        return array
    }

    /**
     * 以块为单位将内容从硬盘读入指定ByteArray中
     * @param array 用于保存从硬盘读入内容的ByteArray
     * @param begin 从硬盘的第几块开始
     * @param number 要读取的块数
     */
    private fun readBlock(array : ByteArray, begin : Int, number : Int) {
        val byteNumber = number * diskInfo.blockSize
        val beginBytePos : Long = (begin * diskInfo.blockSize).toLong()
        diskAccessor.seek(beginBytePos)
        diskAccessor.read(array, 0, byteNumber)
    }

    /**
     * 向文件中写入一个块的内容
     * @param array 要进行写入的数据源，只写入开头的1024字节
     */
    private fun writeOneBlock(array : ByteArray) {
        diskAccessor.write(array, 0, diskInfo.blockSize)
    }

    /**
     * 以块为单位将ByteArray的内容写入硬盘
     * @param array 要被写入硬盘的源ByteArray
     * @param begin 从array中的第几块开始
     * @param number 要写入的块数
     */
    private fun writeBlock(array : ByteArray, begin : Int, number : Int) {
        val byteNumber = number * diskInfo.blockSize
        val beginBytePos : Int = begin * diskInfo.blockSize
        diskAccessor.write(array, beginBytePos, byteNumber)
    }

    /**
     * 将Record信息保存到硬盘中
     * @param id Record的唯一标识，用来指示要写入的硬盘位置
     * @param record 具体的Record内容
     */
    private fun writeRecord(id : Int, record : FileRecord) {
        val blockIndex : Int = id / 8 //一个物理块里可以存放8个文件记录
        diskAccessor.seek(((1 + diskInfo.recordBitmapBlocks + diskInfo.dataBitmapBlocks + blockIndex) * diskInfo.blockSize).toLong())
        val array : ByteArray = readOneBlock()
        val recordIndex : Int = id % 8
        record.toByteArray(array, recordIndex * 128)
        diskAccessor.seek(((1 + diskInfo.recordBitmapBlocks + diskInfo.dataBitmapBlocks + blockIndex) * diskInfo.blockSize).toLong())
        writeOneBlock(array)
    }

    /**
     * 找到第一个空闲的Record位置
     * @return 返回指示位置的整数
     */
    private fun findFirstEmptyRecordIndex() : Int {
        var index : Int = 0
        for (i in recordBitmap.indices) {
            if (recordBitmap[i] != (0).toByte()) {
                index = i
                break
            }
        }
        val bitIndex = index * 8 + findFirstNonzeroBitInByte(recordBitmap[index])
        return bitIndex
    }

    /**
     * 找到第一个空闲的Data块的位置
     * @return 返回指示位置的整数
     */
    private fun findFirstEmptyDataBlockIndex() : Int {
        var index : Int = 0
        for (i in dataBitmap.indices) {
            if (dataBitmap[i] != (0).toByte()) {
                index = i
                break
            }
        }
        val bitIndex = index * 8 + findFirstNonzeroBitInByte(dataBitmap[index])
        return bitIndex
    }

    /**
     * 通用型函数，找出一个字节中从左到右第一个为1的bit位置
     * @return 第一个为1的bit位置
     */
    private fun findFirstNonzeroBitInByte(byte : Byte) : Int {
        var index : Int = 0
        for (i in 7 downTo 0) {
            if (byte and (0x01 shl i).toByte() != (0).toByte()) {
                index = 7 - i
                break
            }
        }
        return index
    }

    /**
     * 对输入路径进行解析得到RecordID
     * @param path 要进行解析的路径
     * @return 解析出的RecordID
     */
    private fun parsePathForRecordID(path : String) : Int {
        if (path == "/") return 0
        var pathList : List<String> = path.split("/")
        pathList = pathList.subList(1, pathList.size)
        var index : Int = 0
        var id : Int = 0
        while (index < pathList.size) {
            val record : FileRecord = getFileRecordFromID(id)
            val recordList : ArrayList<Pair<Int, String>> = getAllRecordNameInFolder(record)
            var flag : Boolean = false //如果for循环中找到对应的record则置为true，没找到则仍未false
            for (i in recordList.indices) {
                if (pathList[index] == recordList[i].second) { //找到名字匹配的record
                    id = recordList[i].first
                    index++
                    flag = true
                    break
                }
            }
            if (flag == false) return -1 //没有找到名字匹配的record，说明路径有问题
        }
        return id
    }

    /**
     * 对输入路径进行解析得到该路径的父亲RecordID
     * @param path 包含要解析的父目录的路径
     * @return 父目录的RecordID
     * 将路径最后的名字串和斜杠去掉，然后用剩余部分调用parsePathForRecordID
     */
    private fun parsePathForFatherRecordID(path : String) : Int {
        val index = path.indexOfLast { it == '/' }
        if (index == 0) return 0
        val fatherPath : String = path.substring(0, index)
        return parsePathForRecordID(fatherPath)
    }

    /**
     * 根据输入的id找到id对应的Record所在的物理块在硬盘中的位置
     * @param id RecordID
     * @return 返回物理块的index
     */
    private fun findRecordBlock(id : Int) : Int {
        val recordBlockOffset : Int = id / 8
        val blockOffset : Int = 1 + diskInfo.recordBitmapBlocks + diskInfo.dataBitmapBlocks + recordBlockOffset
        return blockOffset
    }

    /**
     * 找到record被分配的第index块逻辑块的物理块位置（相对于数据区开头的偏移）
     * @param record 指定的FileRecord
     * @param index 要找的第index逻辑块
     * @return 实际的物理块位置
     */
    private fun findDataBlock(record : FileRecord, index : Int) : Int {
        return if (index <= 12) { //直接指针
            record.directPointer[index - 1]
        } else if (index <= 268) { //一级间接指针
            getDataBlockIndexBySingle(record.singleIndirectPointer, index - 13) //多减1，便于计算
        } else if (index <= 65804) { //二级间接指针
            getDataBlockIndexByDouble(record.doubleIndirectPointer, index - 269)
        } else { //三级间接指针
            getDataBlockIndexByTriple(record.tripleIndirectPointer, index - 65805)
        }
    }

    /**
     * 在一级间接指针里找对应的物理块
     * @param single
     * @param number
     * @return 物理块index
     * 可以直接找到物理块index
     */
    private fun getDataBlockIndexBySingle(single : Int, number : Int) : Int {
        diskAccessor.seek(((diskInfo.firstDataBlockIndex + single) * diskInfo.blockSize).toLong())
        val array : ByteArray = readOneBlock()
        return array.toInt(number * 4)
    }

    /**
     * 在二级间接指针里找对应的物理块
     * @param double
     * @param number
     * @return 物理块index
     * 需要进入一级间接指针
     */
    private fun getDataBlockIndexByDouble(double : Int, number : Int) : Int {
        val singleIndex : Int = number / 256
        diskAccessor.seek(((diskInfo.firstDataBlockIndex + double) * diskInfo.blockSize).toLong())
        val array : ByteArray = readOneBlock()
        val single : Int = array.toInt(singleIndex * 4)
        return getDataBlockIndexBySingle(single, number % 256)
    }

    /**
     * 在三级间接指针里找对应的物理块
     * @param triple
     * @param number
     * @return 物理块index
     * 需要进入二级间接指针
     */
    private fun getDataBlockIndexByTriple(triple : Int, number : Int) : Int {
        val doubleIndex : Int = number / 65536
        diskAccessor.seek(((diskInfo.firstDataBlockIndex + triple) * diskInfo.blockSize).toLong())
        val array : ByteArray = readOneBlock()
        val double : Int = array.toInt(doubleIndex * 4)
        return getDataBlockIndexByDouble(double, number % 65536)
    }

    /**
     * 根据id得到id对应的FileRecord
     * @param id 指定的recordID
     * @return 返回对应的FileRecord
     */
    private fun getFileRecordFromID(id : Int) : FileRecord {
        val recordBlock : Int = findRecordBlock(id)
        val index = id % 8
        diskAccessor.seek((recordBlock * diskInfo.blockSize).toLong())
        val recordBlockArray : ByteArray = readOneBlock()
        val record : FileRecord = FileRecord()
        record.fromByteArray(recordBlockArray, 128 * index)
        return record
    }

    /**
     * 将文件的id和文件名name添加到父文件夹的目录中
     * @param father 父文件夹的record，主要是获取其中的内容指针
     * @param id 要添加文件的id
     * @param name 要添加文件的文件名
     * 在添加之前会先计算空间是否足够，在确保空间足够容纳才会调用，因此没有返回值，默认添加成功
     */
    private fun addItemInFolderTable(father : FileRecord, id : Int, name : String) {
        if (father.fileOccupiedSpace - father.fileSize <= diskInfo.blockSize) { //不分配新块或原来的尾块刚好装满分配一个新块
            val offset : Int = father.fileSize % 1024
            val lastBlockIndex : Int = findDataBlock(father, father.fileOccupiedBlock)
            diskAccessor.seek(((diskInfo.firstDataBlockIndex + lastBlockIndex) * 1024).toLong())
            val array : ByteArray = readOneBlock()
            id.toByteArray(array, offset)
            val stringArray : ByteArray = name.toByteArray(charset("US-ASCII"))
            stringArray.copyToByteArray(array, offset + 4)
            array[offset + 4 + stringArray.size] = 0
            diskAccessor.seek(((diskInfo.firstDataBlockIndex + lastBlockIndex) * 1024).toLong())
            writeOneBlock(array)
            father.fileSize = father.fileSize + 5 + stringArray.size
        }
        else { //原来的尾块未满，但剩余空间不足而分配新块，导致跨块存储
            val lastBlockIndex : Int = findDataBlock(father, father.fileOccupiedBlock)
            val lastButOneBlockIndex : Int = findDataBlock(father, father.fileOccupiedBlock - 1)
            diskAccessor.seek(((diskInfo.firstDataBlockIndex + lastButOneBlockIndex) * 1024).toLong())
            val array1 : ByteArray = readOneBlock()
            diskAccessor.seek(((diskInfo.firstDataBlockIndex + lastBlockIndex) * 1024).toLong())
            val array2 : ByteArray = readOneBlock()
            val array3 : ByteArray = ByteArray(2048)
            array1.copyToByteArray(array3, 0)
            array2.copyToByteArray(array3, 1024)
            val offset : Int = father.fileSize % 1024
            id.toByteArray(array3, offset)
            val stringArray : ByteArray = name.toByteArray(charset("US-ASCII"))
            stringArray.copyToByteArray(array3, offset + 4)
            array3[offset + 4 + stringArray.size] = 0
            diskAccessor.seek(((diskInfo.firstDataBlockIndex + lastButOneBlockIndex) * 1024).toLong())
            writeBlock(array3, 0, 1)
            diskAccessor.seek(((diskInfo.firstDataBlockIndex + lastBlockIndex) * 1024).toLong())
            writeBlock(array3, 1, 1)
            father.fileSize = father.fileSize + 5 + stringArray.size
        }
        writeRecord(father.recordID, father)
    }

    /**
     * 给指定的文件或文件夹的Record分配新块
     * @param record 要分配新块的文件或文件夹
     * @param number 要分配的块数
     * @return 分配是否成功
     * 分配过程和获取record逻辑块对应的物理块位置类似，可能会进入一级二级和三级指针
     */
    private fun allocateBlockToRecord(record : FileRecord, number : Int) : Boolean {
        val beforeSingle : Int = (record.fileOccupiedBlock + 243) / 256
        val beforeDouble : Int = (record.fileOccupiedBlock + 65267) / 65536
        val beforeTriple : Int = (record.fileOccupiedBlock + 16711411) / 16777216
        val afterSingle : Int = (record.fileOccupiedBlock + number + 243) / 256
        val afterDouble : Int = (record.fileOccupiedBlock + number + 65267) / 65536
        val afterTriple : Int = (record.fileOccupiedBlock + number + 16711411) / 16777216
        val dValue : Int = afterSingle - beforeSingle + afterDouble - beforeDouble + afterTriple - beforeTriple //计算由于分配指针带来的额外块分配数
        if (diskInfo.emptyDataBlockNumber < number + dValue) return false //确保空余块数大于等于内容分配块数加指针分配块数

        for (i in 0 until number) {
            val emptyDataBlockIndex : Int = findFirstEmptyDataBlockIndex()
            flipDataBitmapBit(emptyDataBlockIndex) //分配后立刻翻转保证其被占用，之后调用findFirstEmptyDataBlockIndex能够得到新值
            diskAccessor.seek((diskInfo.firstDataBlockIndex * 1024).toLong())
            var array : ByteArray = readOneBlock()
            modifyRecordDataPointer(record, emptyDataBlockIndex)
            diskAccessor.seek((diskInfo.firstDataBlockIndex * 1024).toLong())
            array = readOneBlock()
            record.fileOccupiedBlock = record.fileOccupiedBlock + 1
        }

        //修改硬盘信息
        diskInfo.emptyDataBlockNumber = diskInfo.emptyDataBlockNumber - number - dValue
        diskInfo.allocatedSpace = diskInfo.allocatedSpace + (number + dValue) * 1024
        diskInfo.freeSpace = diskInfo.freeSpace - (number + dValue) * 1024
        saveDiskInfo()
        record.fileOccupiedSpace = record.fileOccupiedBlock * 1024
        writeRecord(record.recordID, record)
        return true
    }

    /**
     * 修改文件Record中的指针项
     * @param record 文件或文件夹的record，目的是获取里面的指针
     * @param emptyBlockIndex 可以被分配的空闲数据块的index
     */
    private fun modifyRecordDataPointer(record : FileRecord, emptyBlockIndex : Int) {
        if (record.fileOccupiedBlock < 12) { //
            modifyRecordDirectPointer(record.directPointer, record.fileOccupiedBlock, emptyBlockIndex)
        }
        else if (record.fileOccupiedBlock == 12) { //需要分配一级指针
            record.singleIndirectPointer = emptyBlockIndex
            val newEmptyBlockIndex : Int = findFirstEmptyDataBlockIndex()
            flipDataBitmapBit(newEmptyBlockIndex) //分配后立刻占用
            modifyRecordSingleIndirectPointer(record.singleIndirectPointer, record.fileOccupiedBlock - 12, newEmptyBlockIndex)
        }
        else if (record.fileOccupiedBlock < 268) { //
            modifyRecordSingleIndirectPointer(record.singleIndirectPointer, record.fileOccupiedBlock - 12, emptyBlockIndex)
        }
        else if (record.fileOccupiedBlock == 268) { //需要分配二级指针
            record.doubleIndirectPointer = emptyBlockIndex
            val newEmptyBlockIndex : Int = findFirstEmptyDataBlockIndex()
            flipDataBitmapBit(newEmptyBlockIndex)
            modifyRecordDoubleIndirectPointer(record.doubleIndirectPointer, record.fileOccupiedBlock - 268, newEmptyBlockIndex)
        }
        else if (record.fileOccupiedBlock < 65804) { //
            modifyRecordDoubleIndirectPointer(record.doubleIndirectPointer, record.fileOccupiedBlock - 268, emptyBlockIndex)
        }
        else if (record.fileOccupiedBlock == 65804) { //需要分配三级指针
            record.tripleIndirectPointer = emptyBlockIndex
            val newEmptyBlockIndex : Int = findFirstEmptyDataBlockIndex()
            flipDataBitmapBit(newEmptyBlockIndex)
            modifyRecordTripleIndirectPointer(record.tripleIndirectPointer, record.fileOccupiedBlock - 65804, newEmptyBlockIndex)
        }
        else { //
            modifyRecordTripleIndirectPointer(record.tripleIndirectPointer, record.fileOccupiedBlock - 65804, emptyBlockIndex)
        }
    }

    /**
     * 修改直接指针
     * @param directPointer 直接指针数组
     * @param offset 新分配块在指针中存储时相对当前指针起点的偏移
     * @param emptyBlockIndex 可以被分配的空闲数据块的index
     */
    private fun modifyRecordDirectPointer(directPointer : IntArray, offset : Int, emptyBlockIndex : Int) {
        directPointer[offset] = emptyBlockIndex
    }

    /**
     * 修改一级指针
     * @param single 一级指针
     * @param offset 新分配块在指针中存储时相对当前指针起点的偏移
     * @param emptyBlockIndex 可以被分配的空闲数据块的index
     */
    private fun modifyRecordSingleIndirectPointer(single : Int, offset : Int, emptyBlockIndex : Int) {
        diskAccessor.seek(((diskInfo.firstDataBlockIndex + single) * 1024).toLong())
        val array : ByteArray = readOneBlock()
        emptyBlockIndex.toByteArray(array, offset * 4)
        diskAccessor.seek(((diskInfo.firstDataBlockIndex + single) * 1024).toLong())
        writeOneBlock(array)
    }

    /**
     * 修改二级指针
     * @param double 二级指针
     * @param offset 新分配块在指针中存储时相对当前指针起点的偏移
     * @param emptyBlockIndex 可以被分配的空闲数据块的index
     */
    private fun modifyRecordDoubleIndirectPointer(double : Int, offset : Int, emptyBlockIndex : Int) {
        val singleOffset : Int = offset % 256
        if (singleOffset == 0) { //需要分配新的一级指针
            diskAccessor.seek(((diskInfo.firstDataBlockIndex + double) * diskInfo.blockSize).toLong())
            val array : ByteArray = readOneBlock()
            val singlePos : Int = offset / 256
            emptyBlockIndex.toByteArray(array, singlePos * 4)
            diskAccessor.seek(((diskInfo.firstDataBlockIndex + double) * diskInfo.blockSize).toLong())
            writeOneBlock(array)
            val newEmptyBlockIndex : Int = findFirstEmptyDataBlockIndex()
            flipDataBitmapBit(newEmptyBlockIndex)
            modifyRecordSingleIndirectPointer(emptyBlockIndex, singleOffset, newEmptyBlockIndex)
        }
        else { //不需要分配新的一级指针
            val singlePos : Int = offset / 256
            diskAccessor.seek(((diskInfo.dataBitmapBlocks + double) * diskInfo.blockSize).toLong())
            val array : ByteArray = readOneBlock()
            val single : Int = array.toInt(singlePos * 4)
            modifyRecordSingleIndirectPointer(single, singleOffset, emptyBlockIndex)
        }
    }

    /**
     * 修改三级指针
     * @param triple 三级指针
     * @param offset 新分配块在指针中存储时相对当前指针起点的偏移
     * @param emptyBlockIndex 可以被分配的空闲数据块的index
     */
    private fun modifyRecordTripleIndirectPointer(triple : Int, offset : Int, emptyBlockIndex : Int) {
        val doubleOffset : Int = offset % 65536
        if (doubleOffset == 0) { //需要分配新的二级指针
            diskAccessor.seek(((diskInfo.firstDataBlockIndex + triple) * diskInfo.blockSize).toLong())
            val array : ByteArray = readOneBlock()
            val doublePos : Int = offset / 65536
            emptyBlockIndex.toByteArray(array, doublePos * 4)
            diskAccessor.seek(((diskInfo.firstDataBlockIndex + triple) * diskInfo.blockSize).toLong())
            writeOneBlock(array)
            val newEmptyBlockIndex : Int = findFirstEmptyDataBlockIndex()
            flipDataBitmapBit(newEmptyBlockIndex)
            modifyRecordDoubleIndirectPointer(emptyBlockIndex, doubleOffset, newEmptyBlockIndex)
        }
        else { //不需要分配新的二级指针
            val doublePos : Int = offset / 256
            diskAccessor.seek(((diskInfo.dataBitmapBlocks + triple) * diskInfo.blockSize).toLong())
            val array : ByteArray = readOneBlock()
            val double : Int = array.toInt(doublePos * 4)
            modifyRecordDoubleIndirectPointer(double, doubleOffset, emptyBlockIndex)
        }
    }

    /**
     * 翻转recordID位置对应的RecordBitmap位，并将翻转后的更改保存到硬盘
     * @param recordID 要翻转的recordID
     */
    private fun flipRecordBitmapBit(recordID : Int) {
        val byteIndex : Int = recordID / 8
        val bitIndex : Int = recordID % 8
        recordBitmap[byteIndex] = recordBitmap[byteIndex] xor (0x01 shl (7 - bitIndex)).toByte()
        val blockIndex : Int = byteIndex / 1024
        diskAccessor.seek(((1 + blockIndex) * 1024).toLong())
        writeBlock(recordBitmap, blockIndex, 1)
    }

    /**
     * 翻转dataBlockIndex位置对应的DataBitmap位，并将翻转后的更改保存到硬盘
     * @param dataBlockIndex 要翻转的dataBlockIndex
     */
    private fun flipDataBitmapBit(dataBlockIndex : Int) {
        val byteIndex : Int = dataBlockIndex / 8
        val bitIndex : Int = dataBlockIndex % 8
        dataBitmap[byteIndex] = dataBitmap[byteIndex] xor (0x01 shl (7 - bitIndex)).toByte()
        val blockIndex : Int = byteIndex / 1024
        diskAccessor.seek(((1 + diskInfo.recordBitmapBlocks + blockIndex) * 1024).toLong())
        writeBlock(dataBitmap, blockIndex, 1)
    }

    /**
     * 创建一个新的文件Record
     * @param recordID 新文件被分配的ID
     * @param fatherID 父文件夹ID
     * @return 返回一个经过初始化的FileRecord
     */
    private fun createNewFileRecord(recordID : Int, fatherID : Int) : FileRecord {
        val record : FileRecord = FileRecord()
        record.recordID = recordID
        record.fileType = 1 //代表文件
        record.fileCreateTime = System.currentTimeMillis().toInt()
        record.fileLastModifiedTime = record.fileCreateTime
        record.fatherRecordID = fatherID
        return record
    }

    /**
     * 创建一个新的文件夹Record
     * @param recordID 新文件夹被分配的ID
     * @param fatherID 父文件夹ID
     * @return 返回一个经过初始化的FileRecord
     */
    private fun createNewFolderRecord(recordID : Int, fatherID : Int) : FileRecord{
        val record : FileRecord = FileRecord()
        if(allocateBlockToRecord(record, 1) == true) { //空间分配成功
            record.recordID = recordID
            record.fileType = 0 //代表文件夹
            record.fileCreateTime = System.currentTimeMillis().toInt()
            record.fileLastModifiedTime = record.fileCreateTime
            record.fatherRecordID = fatherID
            addItemInFolderTable(record, recordID, ".")
            addItemInFolderTable(record, fatherID, "..")
        }
        return record //成功创建该文件夹一定被分配了空间，fileOccupiedBlock不为0，失败则为0
    }

    /**
     * 通过传入的文件夹Record获取该文件夹下的项目
     * @param folder 文件夹record
     * @return 返回一个ArrayList，里面存储着文件夹中每一个record的id和name的对照关系
     */
    private fun getAllRecordNameInFolder(folder : FileRecord) : ArrayList<Pair<Int, String>> {
        val folderArray : ByteArray = ByteArray(folder.fileOccupiedBlock * 1024)
        for (i in 1..folder.fileOccupiedBlock) {
            val blockIndex : Int = findDataBlock(folder, i)
            diskAccessor.seek(((diskInfo.firstDataBlockIndex + blockIndex) * diskInfo.blockSize).toLong())
            val array : ByteArray = readOneBlock()
            array.copyToByteArray(folderArray, (i - 1) * 1024)
        }
        val list : ArrayList<Pair<Int, String>> = ArrayList(0)

        var pos = 0
        while (pos < folder.fileSize) {
            val id : Int = folderArray.toInt(pos)
            pos = pos + 4
            var end : Int = pos
            while ((folderArray[end] != (0).toByte()) and (end < folder.fileSize)) end++
            val name : String = folderArray.toString(pos, end - pos)
            list.add(Pair(id, name))
            pos = end + 1
        }
        return list
    }

    /**
     * 将传入的record分配的数据块回收
     * @param record 要被回收数据块的record
     */
    private fun deleteFileRecordData(record : FileRecord) {
        for (i in 1..record.fileOccupiedBlock) {
            val dataBlockIndex : Int = findDataBlock(record, i)
            flipDataBitmapBit(dataBlockIndex)
        }
        diskInfo.emptyDataBlockNumber = diskInfo.emptyDataBlockNumber + record.fileOccupiedBlock
        diskInfo.allocatedSpace = diskInfo.allocatedSpace - record.fileOccupiedBlock * diskInfo.blockSize
        diskInfo.freeSpace = diskInfo.freeSpace + record.fileOccupiedBlock * diskInfo.blockSize
        saveDiskInfo()
    }

    /**
     * 将id对应的某一表项从父文件夹的目录中删除
     * @param father 父文件夹
     * @param id 要删除表项中id的值
     */
    private fun deleteItemInFolderTable(father : FileRecord, id : Int) {
        val list : ArrayList<Pair<Int, String>> = getAllRecordNameInFolder(father)
        var nameLength : Int = 0
        var index : Int = -1
        for (i in list.indices) {
            if (list[i].first == id) {
                index = i
                nameLength = list[i].second.length
                break
            }
        }

        if (index == -1) return
        list.removeAt(index)
        writeListInFolderTable(list, father)
        father.fileSize = father.fileSize - nameLength - 5
        writeRecord(father.recordID, father)
    }

    /**
     * 将一个储存有id和name对应关系的ArrayList写入一个文件夹的目录中
     * @param list 保存有id和name对应关系的ArrayList
     * @param record 要写入的文件夹record
     */
    private fun writeListInFolderTable(list : ArrayList<Pair<Int, String>>, record : FileRecord) {
        var newFolderSize : Int = 0
        for (i in list.indices) {
            newFolderSize += 5 + list[i].second.length
        }
        val blockNumber : Int = (newFolderSize + 1023) / 1024
        val delta : Int = record.fileOccupiedBlock - blockNumber
        for (i in 0 until delta) { //回收多余的数据块
            val index : Int = findDataBlock(record, record.fileOccupiedBlock)
            flipDataBitmapBit(index)
            record.fileOccupiedBlock = record.fileOccupiedBlock - 1
        }
        diskInfo.emptyDataBlockNumber = diskInfo.emptyDataBlockNumber + delta
        diskInfo.allocatedSpace = diskInfo.allocatedSpace - delta * 1024
        diskInfo.freeSpace = diskInfo.freeSpace + delta * 1024

        val array : ByteArray = folderListToByteArray(list)
        for (i in 1..blockNumber) {
            val blockIndex : Int = findDataBlock(record, i)
            diskAccessor.seek(((diskInfo.firstDataBlockIndex + blockIndex) * 1024).toLong())
            writeBlock(array, i - 1, 1)
        }
    }

    /**
     * 将一个储存有id和name对应关系的ArrayList转换成一个ByteArray
     * @param list 要转换的ArrayList
     * @return 转换后产生的ByteArray
     */
    private fun folderListToByteArray(list : ArrayList<Pair<Int, String>>) : ByteArray {
        var listSize : Int = 0
        for (i in list.indices) {
            listSize += 5 + list[i].second.length
        }
        val blockNumber : Int = (listSize + 1023) / 1024
        val array : ByteArray = ByteArray(blockNumber * 1024)

        var pos : Int = 0
        for (i in list.indices) {
            list[i].first.toByteArray(array, pos)
            pos = pos + 4
            val stringArray : ByteArray = list[i].second.toByteArray(charset("US-ASCII"))
            stringArray.copyToByteArray(array, pos)
            pos = pos + list[i].second.length
            array[pos] = 0
            pos = pos + 1
        }
        return array
    }

    /**
     * 检查一个文件名或文件夹名是否在一个文件夹的目录中出现
     * @param name 要检查的文件名或文件夹名
     * @param record 指定的文件夹record
     * @return 如果存在返回true，不存在返回false
     */
    private fun nameExistInFolder(name : String, record : FileRecord) : Boolean {
        val list : ArrayList<Pair<Int, String>> = getAllRecordNameInFolder(record)
        for (i in list.indices) {
            if (list[i].second == name) return true
        }
        return false
    }

    /**
     * 将输入的record对应的文件夹的数据块回收，并回收文件夹的record
     * @param record 要回收数据块的文件夹
     * 其意义相当于删除该文件夹中的所有目录项以及文件夹本身，如果文件夹中存在文件夹，则进行递归调用进行删除
     */
    private fun deleteFolderByRecord(record : FileRecord) {
        val list : ArrayList<Pair<Int, String>> = getAllRecordNameInFolder(record)
        for (i in list.indices) {
            if ((list[i].second != ".") and (list[i].second != "..")) {
                val r : FileRecord = getFileRecordFromID(list[i].first)
                if (r.fileType == 0) {
                    deleteFolderByRecord(r)
                }
                else {
                    deleteFileRecordData(r)
                    flipRecordBitmapBit(r.recordID)
                    diskInfo.recordNumber = diskInfo.recordNumber + 1
                }
            }
        }
        deleteFileRecordData(record)
        flipRecordBitmapBit(record.recordID)
        diskInfo.recordNumber = diskInfo.recordNumber + 1
        saveDiskInfo()
    }

    override fun fileCreate(fileName : String) : Boolean {
        if (diskInfo.recordNumber == 0) return false
        val fatherID : Int = parsePathForFatherRecordID(fileName)
        val fatherRecord : FileRecord = getFileRecordFromID(fatherID)
        val name : String = fileName.substring(fileName.indexOfLast { it == '/' } + 1, fileName.length)
        if (nameExistInFolder(name, fatherRecord)) return false
        if (fatherRecord.fileSize + name.length + 5 > fatherRecord.fileOccupiedSpace) { //folder分配到的空间不足以添加新文件id和name的对照关系
            val isAllocateSuccess : Boolean = allocateBlockToRecord(fatherRecord, 1)
            if (isAllocateSuccess == true) { //分配成功
                val fileID = findFirstEmptyRecordIndex()
                flipRecordBitmapBit(fileID)
                val fileRecord : FileRecord = createNewFileRecord(fileID, fatherID)
                writeRecord(fileID, fileRecord)
                diskInfo.recordNumber = diskInfo.recordNumber - 1
                saveDiskInfo()
                addItemInFolderTable(fatherRecord, fileID, name)
            }
            else return false
        }
        else { //folder分配到的空间可以容纳新文件id和name的对照关系
            val fileID = findFirstEmptyRecordIndex()
            flipRecordBitmapBit(fileID)
            val fileRecord : FileRecord = createNewFileRecord(fileID, fatherID)
            writeRecord(fileID, fileRecord)
            diskInfo.recordNumber = diskInfo.recordNumber - 1
            saveDiskInfo()
            addItemInFolderTable(fatherRecord, fileID, name)
        }
        return true
    }

    override fun folderCreate(folderName : String) : Boolean {
        if (diskInfo.recordNumber == 0) return false
        val fatherID : Int = parsePathForFatherRecordID(folderName)
        val fatherRecord : FileRecord = getFileRecordFromID(fatherID)
        val name : String = folderName.substring(folderName.indexOfLast { it == '/' } + 1, folderName.length)
        if (nameExistInFolder(name, fatherRecord)) return false
        val folderID = findFirstEmptyRecordIndex()
        flipRecordBitmapBit(folderID)
        val folderRecord : FileRecord = createNewFolderRecord(folderID, fatherID)
        if (folderRecord.fileOccupiedBlock == 0) { //文件夹创建时初始项未分配成功
            flipRecordBitmapBit(folderID) //回收Record资源
            return false
        }
        else { //文件夹创建时初始项分配成功
            if (fatherRecord.fileSize + name.length + 5 > fatherRecord.fileOccupiedSpace) {//father分配到的空间不足以添加新文件id和name的对照关系
                if (allocateBlockToRecord(fatherRecord, 1)) {
                    writeRecord(folderID, folderRecord)
                    diskInfo.recordNumber = diskInfo.recordNumber - 1
                    saveDiskInfo()
                    addItemInFolderTable(fatherRecord, folderID, name)
                }
                else { //分配不成功导致文件夹创建失败，回收所有已分配资源
                    deleteFileRecordData(folderRecord)
                    flipRecordBitmapBit(folderID)
                    return false
                }
            }
            else { //father分配到的空间可以容纳新文件id和name的对照关系
                writeRecord(folderID, folderRecord)
                diskInfo.recordNumber = diskInfo.recordNumber - 1
                saveDiskInfo()
                addItemInFolderTable(fatherRecord, folderID, name)
            }
        }

        return true
    }

    override fun fileDelete(fileName: String) : Boolean {
        val id : Int = parsePathForRecordID(fileName)
        if (id == -1) return false //给定的文件不存在
        for (i in fileDescriptorList.indices) { //文件被打开的状态不允许删除
            if ((fileDescriptorList[i].fileID == id) and (fileDescriptorList[i].counter != 0)) return false
        }
        val fatherID : Int = parsePathForFatherRecordID(fileName)
        val record : FileRecord = getFileRecordFromID(id)
        if (record.fileType == 0) return false //是文件夹而非文件
        val fatherRecord : FileRecord = getFileRecordFromID(fatherID)
        deleteFileRecordData(record)
        deleteItemInFolderTable(fatherRecord, id)
        flipRecordBitmapBit(id)
        diskInfo.recordNumber = diskInfo.recordNumber + 1
        saveDiskInfo()
        return true
    }

    override fun folderDelete(folderName : String) : Boolean {
        val id : Int = parsePathForRecordID(folderName)
        if (id == -1) return false //给定的文件夹不存在
        val name : String = folderName.substring(folderName.indexOfLast { it == '/' } + 1, folderName.length)
        if ((name == ".") or (name == "..")) return false //固定条目不允许删除
        val fatherID : Int = parsePathForFatherRecordID(folderName)
        val record : FileRecord = getFileRecordFromID(id)
        if (record.fileType != 0) return false //是文件而非文件夹
        val fatherRecord : FileRecord = getFileRecordFromID(fatherID)

        deleteFolderByRecord(record)
        deleteItemInFolderTable(fatherRecord, id)
        return true
    }

    override fun fileOpen(filePath: String) : FileHandle {
        val id : Int = parsePathForRecordID(filePath)
        if (id == -1) return -1 //给定的文件不存在
        val record : FileRecord = getFileRecordFromID(id)
        if (record.fileType == 0) return -1 //文件夹不能被打开
        for (i in fileDescriptorList.indices) {
            if (fileDescriptorList[i].fileID == id) { //文件已经被打开，系统中留有句柄
                fileDescriptorList[i].counter++
                return fileDescriptorList[i].fHandle
            }
        }
        for (i in fileDescriptorList.indices) {
            if (fileDescriptorList[i].counter == 0) { //未被使用的文件描述符
                fileDescriptorList[i].fileID = id
                fileDescriptorList[i].rwPointer = 0
                fileDescriptorList[i].counter = 1
                fileDescriptorList[i].fHandle = i
                return fileDescriptorList[i].fHandle
            }
        }
        return -1 //没有剩余的文件描述符
    }

    override fun fileClose(handle : FileHandle) : Boolean {
        if ((handle >= maxFileOpen) or (handle < 0)) return false
        if (fileDescriptorList[handle].counter >= 1) {
            fileDescriptorList[handle].counter--
            return true
        }
        else { //counter = 0说明这个文件没有被打开过
            return false
        }
    }

    override fun fileWrite(handle: FileHandle, src: ByteArray, byteNumber: Int) : Boolean {
        if ((handle >= maxFileOpen) or (handle < 0)) return false //超出范围，无效handle
        if (fileDescriptorList[handle].counter == 0) return false //文件未打开
        if (byteNumber <= 0) return false //写入字节数不符合要求
        if (src.size < byteNumber) return false //写入字节数超过源字节大小
        val record : FileRecord = getFileRecordFromID(fileDescriptorList[handle].fileID)
        if (byteNumber <= record.fileOccupiedSpace) { //不分配新块或回收旧块
            val surplusBlock : Int = record.fileOccupiedBlock - ((byteNumber + 1023) / 1024)
            for (i in 0 until surplusBlock) { //回收多余的物理块
                val blockIndex : Int = findDataBlock(record, record.fileOccupiedBlock)
                flipDataBitmapBit(blockIndex)
                record.fileOccupiedBlock = record.fileOccupiedBlock - 1
                record.fileOccupiedSpace = record.fileOccupiedSpace - 1024
                diskInfo.freeSpace = diskInfo.freeSpace + 1024
                diskInfo.allocatedSpace = diskInfo.allocatedSpace - 1024
                diskInfo.emptyDataBlockNumber = diskInfo.emptyDataBlockNumber + 1
            }
            saveDiskInfo()
            var srcArray : ByteArray = src
            if (src.size % 1024 != 0) { //不是1024的整倍数将其变为1024整倍数
                srcArray = ByteArray(((src.size + 1023) / 1024) * 1024)
                src.copyToByteArray(srcArray, 0)
            }
            for (i in 1..record.fileOccupiedBlock) {
                val dataBlockIndex : Int = findDataBlock(record, i)
                diskAccessor.seek(((diskInfo.firstDataBlockIndex + dataBlockIndex) * 1024).toLong())
                writeBlock(srcArray, i - 1, 1)
            }
            record.fileSize = byteNumber
            record.fileLastModifiedTime = System.currentTimeMillis().toInt()
            writeRecord(record.recordID, record)
            return true
        }
        else { //需要分配新块
            val newBlocks : Int = (byteNumber - record.fileOccupiedSpace + 1023) / 1024
            if(allocateBlockToRecord(record, newBlocks)) { //新块分配成功
                var srcArray : ByteArray = src
                if (src.size % 1024 != 0) { //不是1024的整数倍将其变为1024的整数倍
                    srcArray = ByteArray(((src.size + 1023) / 1024) * 1024)
                    src.copyToByteArray(srcArray, 0)
                }
                for (i in 1..record.fileOccupiedBlock) {
                    val dataBlockIndex : Int = findDataBlock(record, i)
                    diskAccessor.seek(((diskInfo.firstDataBlockIndex + dataBlockIndex) * 1024).toLong())
                    writeBlock(srcArray, i - 1, 1)
                }
                record.fileSize = byteNumber
                record.fileLastModifiedTime = System.currentTimeMillis().toInt()
                writeRecord(record.recordID, record)
                return true
            }
            else return false //新块分配失败
        }
    }

    override fun fileRead(handle: FileHandle, dst : ByteArray, byteNumber: Int) : Int {
        if ((handle >= maxFileOpen) or (handle < 0)) return -1 //超出范围，无效handle
        if (fileDescriptorList[handle].counter == 0) return -1 //文件未打开
        if (byteNumber <= 0) return -1 //读入字节数不符合要求
        if (dst.size < byteNumber) return -1 //读入字节数超过源字节大小
        val record : FileRecord = getFileRecordFromID(fileDescriptorList[handle].fileID)
        if (fileDescriptorList[handle].rwPointer == record.fileSize) return 0 //已经读到文件末尾
        if (record.fileSize - fileDescriptorList[handle].rwPointer >= byteNumber) { //可以读出byteNumber字节
            val beginBlock : Int = (fileDescriptorList[handle].rwPointer / 1024) + 1
            val endBlock : Int = ((fileDescriptorList[handle].rwPointer + byteNumber - 1) / 1024) + 1
            val beginOffset : Int = fileDescriptorList[handle].rwPointer % 1024 //左闭
            val endOffset : Int = (endBlock - beginBlock) * 1024 + (fileDescriptorList[handle].rwPointer + byteNumber - 1) % 1024 //右闭
            val readArray : ByteArray = ByteArray((endBlock - beginBlock + 1) * 1024)
            for (i in 0..endBlock - beginBlock) {
                val blockIndex : Int = findDataBlock(record, beginBlock + i)
                diskAccessor.seek(((diskInfo.firstDataBlockIndex + blockIndex) * 1024).toLong())
                val array : ByteArray = readOneBlock()
                array.copyToByteArray(readArray, i * 1024)
            }
            for (i in beginOffset..endOffset) {
                dst[i - beginOffset] = readArray[i]
            }
            fileDescriptorList[handle].rwPointer = fileDescriptorList[handle].rwPointer + byteNumber
            return byteNumber
        }
        else { //剩余内容不够byteNumber字节
            val beginBlock : Int = (fileDescriptorList[handle].rwPointer / 1024) + 1
            val beginOffset : Int = fileDescriptorList[handle].rwPointer % 1024 //左闭
            val endBlock : Int = record.fileOccupiedBlock
            val endOffset : Int = record.fileSize - 1
            val readArray : ByteArray = ByteArray((endBlock - beginBlock + 1) * 1024)
            for (i in 0..endBlock - beginBlock) {
                val blockIndex : Int = findDataBlock(record, beginBlock + i)
                diskAccessor.seek(((diskInfo.firstDataBlockIndex + blockIndex) * 1024).toLong())
                val array : ByteArray = readOneBlock()
                array.copyToByteArray(readArray, i * 1024)
            }
            for (i in beginOffset..endOffset) {
                dst[i - beginOffset] = readArray[i]
            }
            fileDescriptorList[handle].rwPointer = record.fileSize //读到文件结尾
            return endOffset - beginOffset + 1
        }
    }

    override fun fileRename(fileName: String, fileNameNew: String) : Boolean {
        val id : Int = parsePathForRecordID(fileName)
        if (id == -1) return false
        val name : String = fileName.substring(fileName.indexOfLast { it == '/' } + 1, fileName.length)
        val newName : String = fileNameNew.substring(fileNameNew.indexOfLast { it == '/' } + 1, fileNameNew.length)
        val fatherID : Int = parsePathForFatherRecordID(fileName)
        val fatherRecord : FileRecord = getFileRecordFromID(fatherID)
        val list : ArrayList<Pair<Int, String>> = getAllRecordNameInFolder(fatherRecord)
        for (i in list.indices) {
            if (list[i].second == newName) return false
        }
        for (i in list.indices) {
            if (list[i].first == id) list.removeAt(i)
        }
        list.add(Pair(id, newName))

        if (fatherRecord.fileSize - name.length + newName.length > fatherRecord.fileOccupiedSpace) { //名字加长导致的存储空间不足
            if (allocateBlockToRecord(fatherRecord, 1)) {
                writeListInFolderTable(list, fatherRecord)
                fatherRecord.fileSize = fatherRecord.fileSize - name.length + newName.length
                writeRecord(fatherID, fatherRecord)
            }
            else {
                return false
            }
        }
        else { //分配的存储空间足够存储字符
            writeListInFolderTable(list, fatherRecord)
            fatherRecord.fileSize = fatherRecord.fileSize - name.length + newName.length
            writeRecord(fatherID, fatherRecord)
        }
        return true
    }

    override fun getFileInformation(filePath : String) : FileInfo {
        val info : FileInfo = FileInfo(filePath)
        val id : Int = parsePathForRecordID(filePath)
        if (id == -1) { //文件不存在
            info.recordID = -1
            return info
        }
        val record : FileRecord = getFileRecordFromID(id)
        info.recordID = id
        info.fileType = record.fileType
        info.fileSize = record.fileSize
        info.fileOccupiedSpace = record.fileOccupiedSpace
        info.fileCreateTime = record.fileCreateTime
        info.fileLastModifiedTime = record.fileLastModifiedTime
        return info
    }

    override fun getAllRecordNameInFolderByName(name : String) : ArrayList<Pair<Int, String>> {
        val folderID : Int = parsePathForRecordID(name)
        val folderRecord : FileRecord = getFileRecordFromID(folderID)
        if (folderRecord.fileType != 0) { //不是文件夹
            return ArrayList(0)
        }
        return getAllRecordNameInFolder(folderRecord)
    }
}//End of team.os.FileSystem.FileSystem

/**
 * 记录文件系统的描述信息
 * @property blockSize 物理块大小
 * @property blockNumber 磁盘物理块的数量
 * @property emptyDataBlockNumber 数据区空闲的块数
 * @property allocatedSpace 磁盘已分配的空间大小
 * @property freeSpace 磁盘未分配的空间大小
 * @property recordNumber 文件记录的数量
<<<<<<< HEAD
=======
 * @property recordBlockNumber 记录实际文件记录所用的块数
>>>>>>> origin/master
 * @property recordBitmapBlocks 用于标记文件记录未分配位置的位图块数
 * @property dataBitmapBlocks 用于标记数据区未分配物理块的位图块数
 * @property firstDataBlockIndex 标记数据区第一块物理块的位置
 */
class DiskInformation() {
    var blockSize : Int = 0
    var blockNumber : Int = 0
    var emptyDataBlockNumber : Int = 0
    var allocatedSpace : Int = 0
    var freeSpace : Int = 0
    var recordNumber : Int = 0
    var recordBlockNumber : Int = 0
    var recordBitmapBlocks : Int = 0
    var dataBitmapBlocks : Int = 0
    var firstDataBlockIndex : Int = 0

    fun toByteArray() : ByteArray {
        val array : ByteArray = ByteArray(1024)
        blockSize.toByteArray(array, 0)
        blockNumber.toByteArray(array, 4)
        emptyDataBlockNumber.toByteArray(array, 8)
        allocatedSpace.toByteArray(array, 12)
        freeSpace.toByteArray(array, 16)
        recordNumber.toByteArray(array, 20)
        recordBlockNumber.toByteArray(array, 24)
        recordBitmapBlocks.toByteArray(array, 28)
        dataBitmapBlocks.toByteArray(array, 32)
        firstDataBlockIndex.toByteArray(array, 36)
        return array
    }

    fun fromByteArray(array : ByteArray) {
        blockSize = array.toInt(0)
        blockNumber = array.toInt(4)
        emptyDataBlockNumber = array.toInt(8)
        allocatedSpace = array.toInt(12)
        freeSpace = array.toInt(16)
        recordNumber = array.toInt(20)
        recordBlockNumber = array.toInt(24)
        recordBitmapBlocks = array.toInt(28)
        dataBitmapBlocks = array.toInt(32)
        firstDataBlockIndex = array.toInt(36)
    }
}

/**
 * 将Int转化为长度为4的ByteArray
 */
fun Int.toByteArray() : ByteArray {
    val array : ByteArray = ByteArray(4)
    array[0] = ((this shr 24) and 0xff).toByte()
    array[1] = ((this shr 16) and 0xff).toByte()
    array[2] = ((this shr 8) and 0xff).toByte()
    array[3] = (this and 0xff).toByte()
    return array
}

/**
 * 将Int转换为Bytes并保存到指定ByteArray的指定位置
 * @param array 要保存到的指定ByteArray
 * @param pos 在ByteArray中的指定位置
 */
fun Int.toByteArray(array : ByteArray, pos : Int) {
    array[pos] = ((this shr 24) and 0xff).toByte()
    array[pos+1] = ((this shr 16) and 0xff).toByte()
    array[pos+2] = ((this shr 8) and 0xff).toByte()
    array[pos+3] = (this and 0xff).toByte()
}

/**
 * 将长度为4的ByteArray转换为Int
 */
fun ByteArray.toInt() : Int {
    var integer : Int = 0
    integer = integer or (this[0].toInt() and 0xff)
    integer = integer shl 8
    integer = integer or (this[1].toInt() and 0xff)
    integer = integer shl 8
    integer = integer or (this[2].toInt() and 0xff)
    integer = integer shl 8
    integer = integer or (this[3].toInt() and 0xff)
    return integer
}

/**
 * 将ByteArray中特定位置开始的4字节转换为Int
 * @param begin ByteArray中的指定位置
 * @return 从begin开始的4字节转换成的Int
 */
fun ByteArray.toInt(begin : Int) : Int {
    var integer : Int = 0
    integer = integer or (this[begin].toInt() and 0xff)
    integer = integer shl 8
    integer = integer or (this[begin+1].toInt() and 0xff)
    integer = integer shl 8
    integer = integer or (this[begin+2].toInt() and 0xff)
    integer = integer shl 8
    integer = integer or (this[begin+3].toInt() and 0xff)
    return integer
}

/**
 * 将ByteArray复制到另一个ByteArray中
 * @param array 复制目的地ByteArray
 * @param begin 从目的地ByteArray的何处开始
 */
fun ByteArray.copyToByteArray(array : ByteArray, begin : Int) {
    for (i in this.indices) {
        array[begin + i] = this[i]
    }
}

/**
 * 将ByteArray中指定的区域转换成字符串
 * @param begin 从ByteArray的何处开始
 * @param number 字符串的长度
 */
fun ByteArray.toString(begin : Int, number : Int) : String {
    val array : ByteArray = ByteArray(number)
    for (i in 0 until number) {
        array[i] = this[begin + i]
    }
    return array.toString(charset("US-ASCII"))
}

/**
 * 文件记录(team.os.FileSystem.FileRecord)是文件记录区组成的基本单位，保存与文件有关但与具体内容无关的相关信息
 * @property recordID 创建文件时系统给文件分配的系统内唯一指定标识
 * @property fileType 文件类型，0为文件夹，其他数字可代表文件的具体类型
 * @property fileSize 文件大小，单位为字节
 * @property fileOccupiedSpace 文件占用的空间大小，单位为字节，为blockSize的整数倍
 * @property fileOccupiedBlock 文件占用的物理块数量
 * @property fileCreateTime 文件创建的时间(从1970年1月1日0时算起)，单位为秒
 * @property fileLastModifiedTime 文件最后被修改的时间，基准和单位同上
 * @property fatherRecordID 父文件夹的recordID
 * 采用索引方式记录文件内容的具体物理块
 * @property directPointer 12个指针直接指向保存文件内容的物理块，Int保存的是物理块号
 * @property singleIndirectPointer 一级间接索引指针
 * @property doubleIndirectPointer 二级间接索引指针
 * @property tripleIndirectPointer 三级间接索引指针
 */
class FileRecord() {
    var recordID : Int = 0
    var fileType : Int = 0
    var fileSize : Int = 0
    var fileOccupiedSpace : Int = 0
    var fileOccupiedBlock : Int = 0
    var fileCreateTime : Int = 0
    var fileLastModifiedTime : Int = 0
    var fatherRecordID : Int = 0
    var directPointer : IntArray = IntArray(12, fun(i : Int) : Int { return 0 })
    var singleIndirectPointer : Int = 0
    var doubleIndirectPointer : Int = 0
    var tripleIndirectPointer : Int = 0

    fun fromByteArray(array : ByteArray, begin : Int) {
        recordID = array.toInt(begin + 0)
        fileType = array.toInt(begin + 4)
        fileSize = array.toInt(begin + 8)
        fileOccupiedSpace = array.toInt(begin + 12)
        fileOccupiedBlock = array.toInt(begin + 16)
        fileCreateTime = array.toInt(begin + 20)
        fileLastModifiedTime = array.toInt(begin + 24)
        for (i in directPointer.indices)
            directPointer[i] = array.toInt(begin + 28 + 4 * i)
        singleIndirectPointer = array.toInt(begin + 76)
        doubleIndirectPointer = array.toInt(begin + 80)
        tripleIndirectPointer = array.toInt(begin + 84)
        fatherRecordID = array.toInt(begin + 88)
    }

    fun toByteArray(array : ByteArray, begin : Int) {
        recordID.toByteArray(array, begin)
        fileType.toByteArray(array, begin + 4)
        fileSize.toByteArray(array, begin + 8)
        fileOccupiedSpace.toByteArray(array, begin + 12)
        fileOccupiedBlock.toByteArray(array, begin + 16)
        fileCreateTime.toByteArray(array, begin + 20)
        fileLastModifiedTime.toByteArray(array, begin + 24)
        for (i in directPointer.indices)
            directPointer[i].toByteArray(array, begin + 28 + 4 * i)
        singleIndirectPointer.toByteArray(array, begin + 76)
        doubleIndirectPointer.toByteArray(array, begin + 80)
        tripleIndirectPointer.toByteArray(array, begin + 84)
        fatherRecordID.toByteArray(array, begin + 88)
    }
}
