package team.os.fileSystem

//FileHandle文件句柄是当文件处于打开状态时系统为其保存的唯一标识符
typealias FileHandle = Int

sealed interface IFileSystem {
    /**创建文件
     * @param fileName 要创建的文件名
     * @return 创建是否成功
     */
    fun fileCreate(fileName: String): Boolean

    /**删除文件
     * @param fileName 要删除的文件名
     * @return 删除是否成功
     */
    fun fileDelete(fileName: String): Boolean

    /**创建文件夹
     * @param folderName 要创建的文件夹名
     * @return 创建是否成功
     */
    fun folderCreate(folderName: String): Boolean

    /**删除文件夹
     * @param folderName 要删除的文件夹名
     * @return 删除是否成功
     */
    fun folderDelete(folderName: String): Boolean

    /**打开文件
     * @param filePath 要打开的文件所在路径
     * @return 打开文件的句柄，为null则打开失败
     */
    fun fileOpen(filePath: String): FileHandle

    /**关闭文件
     * @param handle 要关闭的文件句柄
     * @return 关闭是否成功
     */
    fun fileClose(handle: FileHandle): Boolean

    /**写入文件
     * @param handle 要写入的文件句柄
     * @param src 要写入的内容首地址
     * @param byteNumber 要写入的字节数
     * @return 写入是否成功
     */
    fun fileWrite(handle: FileHandle, src: ByteArray, byteNumber: Int): Boolean

    /**读出文件
     * @param handle 要读出的文件句柄
     * @param dst 保存读出内容的字节数组
     * @param byteNumber 要读出的字节数
     * @return 读出的实际字节数
     */
    fun fileRead(handle: FileHandle, dst: ByteArray, byteNumber: Int): Int

    /**重命名文件
     * @param fileName 要重命名的文件
     * @param fileNameNew 重命名后的文件名
     * @return 重命名是否成功
     */
    fun fileRename(fileName: String, fileNameNew: String): Boolean

    /**获取文件信息
     * @param filePath 文件路径
     * @return 包含文件各类信息的结构体（创建时间、版本等）
     */
    fun getFileInformation(filePath: String): FileInfo

    /**获取文件夹内所有内容
     * @param folder 要获取的文件夹结点
     * @return 文件夹内所有结点的名称数组
     */
    fun getAllRecordNameInFolderByName(name: String): ArrayList<Pair<Int, String>> //完成
}

/**文件描述符：保存打开文件的相关信息
 * @property fileID
 * @property fHandle 被打开文件在系统中留存的句柄
 * @property rwPointer 读写指针
 * @property counter 引用计数
 */
class FileDescriptor {
    var fileID: Int = 0
    var fHandle: FileHandle = 0
    var rwPointer: Int = 0
    var counter: Int = 0
}

/**记录结点：文件系统中文件和文件夹的统一抽象结构
 * @property nodeName 结点名称
 * @property type 结点类型，0为文件，1为文件夹
 * @property createTime 结点创建时间
 * @property latestModifiedTime 结点最后修改时间
 * @property directPointer （若是文件）指向物理块的直接指针
 * @property singleIndirectPointer （若是文件）一级物理块指针
 * @property doubleIndirectPointer （若是文件）二级物理块指针
 * @property tripleIndirectPointer （若是文件）三级物理块指针
 * @property childNode 若该结点为文件夹，则表示文件夹下的子结点
 */
class RecordNode(var nodeName: String, var type: Int) {
    var createTime: Long = 0
    var latestModifiedTime: Long = 0
    var directPointer: MutableList<Int> = mutableListOf()
    var singleIndirectPointer: Int = 0
    var doubleIndirectPointer: Int = 0
    var tripleIndirectPointer: Int = 0
    var childNode: MutableList<RecordNode> = mutableListOf()
}

/**文件信息：用于返回文件信息查询的数据结构
 * @property filePath 文件路径
 * @property recordID 系统识别文件的数字标记
 * @property fileType 文件类型
 * @property fileSize 文件大小
 * @property fileOccupiedSpace 文件占用的空间大小
 * @property fileCreateTime 文件创建时间（从1970年1月1日0时开始计算的秒数）
 * @property fileLastModifiedTime 文件最后修改时间
 */
data class FileInfo(var filePath: String) {
    var recordID: Int = 0
    var fileType: Int = 0
    var fileSize: Int = 0
    var fileOccupiedSpace: Int = 0
    var fileCreateTime: Int = 0
    var fileLastModifiedTime: Int = 0
}
