<template>
  <el-form ref="userRef" :model="user" :rules="rules" label-width="80px">
    <el-form-item label="用户昵称" prop="nickName">
      <el-input v-model="user.nickName" maxlength="30" />
    </el-form-item>
    <el-form-item label="手机号码" prop="phonenumber">
      <el-input v-model="user.phonenumber" maxlength="11" />
    </el-form-item>
    <el-form-item label="邮箱" prop="email">
      <el-input v-model="user.email" maxlength="50" />
    </el-form-item>
    <el-form-item label-width="120px" class="custom-label">
      <template #label>
        <span
            class="help-icon"
            @click="showHelpDialog"
            title="如何配置QQ邮箱授权码"
        >
          ?
        </span>
        <span>邮箱密钥</span>
      </template>
      <el-input v-model="user.emailAuthorizationCode" maxlength="50" />
    </el-form-item>
    <el-form-item label="性别">
      <el-radio-group v-model="user.sex">
        <el-radio label="0">男</el-radio>
        <el-radio label="1">女</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submit">保存</el-button>
      <el-button type="danger" @click="close">关闭</el-button>
    </el-form-item>
  </el-form>

  <el-dialog
      title="如何配置QQ邮箱授权码"
      v-model="dialogVisible"
      width="50%"
  >
    <ol>
      <li>登录你的QQ邮箱</li>
      <li>点击"设置" > "账户"</li>
      <li>找到"POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务"并开启</li>
      <li>点击"生成授权码"</li>
      <li>按照提示完成身份验证</li>
      <li>复制生成的授权码</li>
      <li>将授权码粘贴到本页面的"邮箱密钥"输入框中</li>
    </ol>
    <p>注意：请妥善保管你的授权码，不要泄露给他人。</p>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { updateUserProfile } from "@/api/system/user";

const props = defineProps({
  user: {
    type: Object,
    required: true
  }
});

const userRef = ref(null);
const dialogVisible = ref(false);

const rules = reactive({
  nickName: [{ required: true, message: "用户昵称不能为空", trigger: "blur" }],
  email: [
    { required: true, message: "邮箱地址不能为空", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }
  ],
  phonenumber: [
    { required: true, message: "手机号码不能为空", trigger: "blur" },
    { pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }
  ],
});

const submit = () => {
  userRef.value.validate(valid => {
    if (valid) {
      updateUserProfile(props.user).then(() => {
        ElMessage.success("修改成功");
      });
    }
  });
}

const close = () => {
  // 这里需要根据你的路由设置来实现关闭页面的逻辑
  // 例如：router.back()
}

const showHelpDialog = () => {
  dialogVisible.value = true;
}
</script>

<style scoped>
.custom-label :deep(.el-form-item__label) {
  display: flex;
  align-items: center;
  white-space: nowrap;
}

.help-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  font-size: 12px;
  font-weight: bold;
  margin-right: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.help-icon:hover {
  background-color: #66b1ff;
  transform: scale(1.1);
}
</style>