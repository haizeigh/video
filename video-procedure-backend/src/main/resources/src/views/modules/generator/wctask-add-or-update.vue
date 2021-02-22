<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="摄像头编号

" prop="cameraNo">
      <el-input v-model="dataForm.cameraNo" placeholder="摄像头编号

"></el-input>
    </el-form-item>
    <el-form-item label="视频的开始时间

" prop="videoStartTime">
      <el-input v-model="dataForm.videoStartTime" placeholder="视频的开始时间

"></el-input>
    </el-form-item>
    <el-form-item label="视频的结束时间" prop="videoEndTime">
      <el-input v-model="dataForm.videoEndTime" placeholder="视频的结束时间"></el-input>
    </el-form-item>
    <el-form-item label="每秒的帧数" prop="frame">
      <el-input v-model="dataForm.frame" placeholder="每秒的帧数"></el-input>
    </el-form-item>
    <el-form-item label="任务的开始时间" prop="taskStartTime">
      <el-input v-model="dataForm.taskStartTime" placeholder="任务的开始时间"></el-input>
    </el-form-item>
    <el-form-item label="任务的结束时间" prop="taskEndTime">
      <el-input v-model="dataForm.taskEndTime" placeholder="任务的结束时间"></el-input>
    </el-form-item>
    <el-form-item label="任务状态

" prop="taskStatus">
      <el-input v-model="dataForm.taskStatus" placeholder="任务状态

"></el-input>
    </el-form-item>
    <el-form-item label="任务备注" prop="context">
      <el-input v-model="dataForm.context" placeholder="任务备注"></el-input>
    </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
        dataForm: {
          taskNo: 0,
          cameraNo: '',
          videoStartTime: '',
          videoEndTime: '',
          frame: '',
          taskStartTime: '',
          taskEndTime: '',
          taskStatus: '',
          context: ''
        },
        dataRule: {
          cameraNo: [
            { required: true, message: '摄像头编号

不能为空', trigger: 'blur' }
          ],
          videoStartTime: [
            { required: true, message: '视频的开始时间

不能为空', trigger: 'blur' }
          ],
          videoEndTime: [
            { required: true, message: '视频的结束时间不能为空', trigger: 'blur' }
          ],
          frame: [
            { required: true, message: '每秒的帧数不能为空', trigger: 'blur' }
          ],
          taskStartTime: [
            { required: true, message: '任务的开始时间不能为空', trigger: 'blur' }
          ],
          taskEndTime: [
            { required: true, message: '任务的结束时间不能为空', trigger: 'blur' }
          ],
          taskStatus: [
            { required: true, message: '任务状态

不能为空', trigger: 'blur' }
          ],
          context: [
            { required: true, message: '任务备注不能为空', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      init (id) {
        this.dataForm.taskNo = id || 0
        this.visible = true
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.taskNo) {
            this.$http({
              url: this.$http.adornUrl(`/generator/wctask/info/${this.dataForm.taskNo}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.cameraNo = data.wcTask.cameraNo
                this.dataForm.videoStartTime = data.wcTask.videoStartTime
                this.dataForm.videoEndTime = data.wcTask.videoEndTime
                this.dataForm.frame = data.wcTask.frame
                this.dataForm.taskStartTime = data.wcTask.taskStartTime
                this.dataForm.taskEndTime = data.wcTask.taskEndTime
                this.dataForm.taskStatus = data.wcTask.taskStatus
                this.dataForm.context = data.wcTask.context
              }
            })
          }
        })
      },
      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/generator/wctask/${!this.dataForm.taskNo ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'taskNo': this.dataForm.taskNo || undefined,
                'cameraNo': this.dataForm.cameraNo,
                'videoStartTime': this.dataForm.videoStartTime,
                'videoEndTime': this.dataForm.videoEndTime,
                'frame': this.dataForm.frame,
                'taskStartTime': this.dataForm.taskStartTime,
                'taskEndTime': this.dataForm.taskEndTime,
                'taskStatus': this.dataForm.taskStatus,
                'context': this.dataForm.context
              })
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: '操作成功',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      }
    }
  }
</script>
