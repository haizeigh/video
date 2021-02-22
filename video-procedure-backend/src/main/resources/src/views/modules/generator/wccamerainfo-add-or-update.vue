<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="摄像头编号" prop="cameraNo">
      <el-input v-model="dataForm.cameraNo" placeholder="摄像头编号"></el-input>
    </el-form-item>
    <el-form-item label="01表示主码流，02是子码流" prop="streamNo">
      <el-input v-model="dataForm.streamNo" placeholder="01表示主码流，02是子码流"></el-input>
    </el-form-item>
    <el-form-item label="nvr的ip" prop="nvrIp">
      <el-input v-model="dataForm.nvrIp" placeholder="nvr的ip"></el-input>
    </el-form-item>
    <el-form-item label="nvr的开放端口" prop="nvrPort">
      <el-input v-model="dataForm.nvrPort" placeholder="nvr的开放端口"></el-input>
    </el-form-item>
    <el-form-item label="用户名" prop="userName">
      <el-input v-model="dataForm.userName" placeholder="用户名"></el-input>
    </el-form-item>
    <el-form-item label="密码" prop="passwd">
      <el-input v-model="dataForm.passwd" placeholder="密码"></el-input>
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
          id: 0,
          cameraNo: '',
          streamNo: '',
          nvrIp: '',
          nvrPort: '',
          userName: '',
          passwd: ''
        },
        dataRule: {
          cameraNo: [
            { required: true, message: '摄像头编号不能为空', trigger: 'blur' }
          ],
          streamNo: [
            { required: true, message: '01表示主码流，02是子码流不能为空', trigger: 'blur' }
          ],
          nvrIp: [
            { required: true, message: 'nvr的ip不能为空', trigger: 'blur' }
          ],
          nvrPort: [
            { required: true, message: 'nvr的开放端口不能为空', trigger: 'blur' }
          ],
          userName: [
            { required: true, message: '用户名不能为空', trigger: 'blur' }
          ],
          passwd: [
            { required: true, message: '密码不能为空', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http({
              url: this.$http.adornUrl(`/generator/wccamerainfo/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.cameraNo = data.wcCameraInfo.cameraNo
                this.dataForm.streamNo = data.wcCameraInfo.streamNo
                this.dataForm.nvrIp = data.wcCameraInfo.nvrIp
                this.dataForm.nvrPort = data.wcCameraInfo.nvrPort
                this.dataForm.userName = data.wcCameraInfo.userName
                this.dataForm.passwd = data.wcCameraInfo.passwd
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
              url: this.$http.adornUrl(`/generator/wccamerainfo/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'cameraNo': this.dataForm.cameraNo,
                'streamNo': this.dataForm.streamNo,
                'nvrIp': this.dataForm.nvrIp,
                'nvrPort': this.dataForm.nvrPort,
                'userName': this.dataForm.userName,
                'passwd': this.dataForm.passwd
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
