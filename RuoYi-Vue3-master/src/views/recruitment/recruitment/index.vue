<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--用户数据-->
      <el-col :span="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="岗位类型" prop="positionType">
            <el-select
                v-model="queryParams.positionType"
                placeholder="请输入岗位类型"
                clearable
                style="width: 240px"
            >
              <el-option
                  v-for="option in form.positionOptions"
                  :key="option"
                  :label="option"
                  :value="option"
              />
              </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
                type="primary"
                plain
                icon="Plus"
                @click="handleAdd"
                v-hasPermi="['system:user:add']"
            >新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
                type="danger"
                plain
                icon="Delete"
                :disabled="multiple"
                @click="handleDeleteList"
                v-hasPermi="['system:user:remove']"
            >删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
                type="warning"
                plain
                icon="Download"
                @click="handleExport"
                v-hasPermi="['system:user:export']"
            >导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange" class="full-width-table">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" v-if="columns[0].visible">
            <template #default="scope">
              {{scope.$index+1}}
            </template>
          </el-table-column>
          <el-table-column label="企业" align="center" key="enterprise" prop="enterprise" v-if="columns[3].visible" :show-overflow-tooltip="true" />
          <el-table-column label="岗位" align="center" key="position" prop="position" v-if="columns[3].visible" :show-overflow-tooltip="true" />
          <el-table-column label="岗位类型" align="center" key="positionType" prop="positionType" v-if="columns[3].visible" :show-overflow-tooltip="true" />
          <el-table-column label="薪资范围" align="center" key="salaryRange" prop="salaryRange" v-if="columns[4].visible" width="120" />
          <el-table-column label="工作地点" align="center" key="location" prop="location" v-if="columns[5].visible" :show-overflow-tooltip="true" />
          <el-table-column label="岗位状态" align="center" key="jobStatus" prop="jobStatus" v-if="columns[1].visible">
          </el-table-column>
          <el-table-column label="岗位描述" align="center" prop="jobDescription" v-if="columns[1].visible" width="160">
            <template #default="scope">
              <span>{{ scope.row.jobDescription }}</span>
            </template>
          </el-table-column>
          <el-table-column label="是否启用" align="center" key="isActive" prop="isActive" v-if="columns[1].visible">
          </el-table-column>
          <el-table-column label="记录创建时间" align="center" prop="createdAt" v-if="columns[1].visible" width="160">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="记录最后更新时间" align="center" prop="updatedAt" v-if="columns[1].visible" width="160">
            <template #default="scope">
              <span>{{ parseTime(scope.row.updatedAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-tooltip content="修改" placement="top" >
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" ></el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top" >
                <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
              </el-tooltip>

            </template>
          </el-table-column>
        </el-table>


        <pagination
            v-show="total > 0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="getList"
        />
      </el-col>
    </el-row>

    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="userRef" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="企业" prop="enterprise">
              <el-select v-model="form.enterprise" placeholder="请选择企业">
                <el-option
                    v-for="option in form.enterpriseOptions"
                    :key="option"
                    :label="option"
                    :value="option"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="position">
              <el-select v-model="form.position" placeholder="请选择岗位">
                <el-option
                    v-for="option in form.positionOptions"
                    :key="option"
                    :label="option"
                    :value="option"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="岗位类型" prop="positionType">
              <el-select v-model="form.positionType" placeholder="请选择岗位类型">
                <el-option label="全职" value="Full-time"></el-option>
                <el-option label="兼职" value="Part-time"></el-option>
                <el-option label="实习" value="Intern"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资范围" prop="salaryRange">
              <el-input v-model="form.salaryRange" placeholder="请输入薪资范围" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="工作地点" prop="location">
              <el-input v-model="form.location" placeholder="请输入工作地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位状态" prop="jobStatus">
              <el-select v-model="form.jobStatus" placeholder="请选择岗位状态">
                <el-option label="开放中" value="Open"></el-option>
                <el-option label="已关闭" value="Closed"></el-option>
                <el-option label="暂停中" value="On Hold"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="岗位描述" prop="jobDescription">
              <el-input v-model="form.jobDescription" placeholder="请输入岗位描述" type="textarea" />
            </el-form-item>
          </el-col>

        </el-row>
        <el-row>

          <el-col :span="12">
            <el-form-item label="是否启用" prop="isActive">
              <el-switch v-model="form.isActive" active-text="启用" inactive-text="禁用"   active-value="1"
                         inactive-value="0"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>



    <!-- 用户导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body>
      <el-upload
          ref="uploadRef"
          :limit="1"
          accept=".xlsx, .xls"
          :headers="upload.headers"
          :action="upload.url + '?updateSupport=' + upload.updateSupport"
          :disabled="upload.isUploading"
          :on-progress="handleFileUploadProgress"
          :on-success="handleFileSuccess"
          :auto-upload="false"
          drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <div class="el-upload__tip">
              <el-checkbox v-model="upload.updateSupport" />是否更新已经存在的用户数据
            </div>
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="importTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="User">
import { getToken } from "@/utils/auth";
import { changeUserStatus, listUser, resetUserPwd, delUser, getUser, updateUser, addUser, deptTreeSelect } from "@/api/system/user";
import {
  addRecruitment,
  delRecruitment,
  getRecruitment,
  listRecruitment,
  updateRecruitment
} from "@/api/recruitment/recruitment";
import {onMounted, ref} from "vue";
import {deleteIndustryList, getIndustryOptions} from "../../../api/system/industry";
import {getJobPositionOptions} from "../../../api/recruitment/post";
import {getEnterpriseOptions} from "../../../api/system/enterprise";
import {deleteJobEnterprisePositions} from "../../../api/recruitment/recruitment";

const router = useRouter();
const { proxy } = getCurrentInstance();
const { sys_normal_disable, sys_user_sex } = proxy.useDict("sys_normal_disable", "sys_user_sex");

const userList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const deptName = ref("");
const deptOptions = ref(undefined);
const initPassword = ref(undefined);
const postOptions = ref([]);
const roleOptions = ref([]);
/*** 用户导入参数 */
const upload = reactive({
  // 是否显示弹出层（用户导入）
  open: false,
  // 弹出层标题（用户导入）
  title: "",
  // 是否禁用上传
  isUploading: false,
  // 是否更新已经存在的用户数据
  updateSupport: 0,
  // 设置上传的请求头部
  headers: { Authorization: "Bearer " + getToken() },
  // 上传的地址
  url: import.meta.env.VITE_APP_BASE_API + "/system/user/importData"
});
// 列显隐信息
const columns = ref([
  { key: 0, label: `用户编号`, visible: true },
  { key: 1, label: `用户名称`, visible: true },
  { key: 2, label: `用户昵称`, visible: true },
  { key: 3, label: `部门`, visible: true },
  { key: 4, label: `手机号码`, visible: true },
  { key: 5, label: `状态`, visible: true },
  { key: 6, label: `创建时间`, visible: true }
]);

const data = reactive({
  form: {
    enterpriseJobId: undefined,
    enterpriseId: undefined,
    positionType: undefined,
    enterpriseName: undefined,
    jobPosition: undefined,
    jobStatus: undefined,
    jobDescription: undefined,
    isActive: undefined,
    enterpriseJobName: undefined,
    location: undefined,
    enterpriseOptions: [],
    positionOptions: [],
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phonenumber: undefined,
    status: undefined,
    deptId: undefined
  },
  rules: {
    positionType: [
      { required: true, message: "岗位类型不能为空", trigger: "change" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);


/** 查询用户列表 */
function getList() {
  loading.value = true;
  listRecruitment(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {
    loading.value = false;

    // 处理 isActive 字段
    const processedRecords = res.records.map(item => ({
      ...item,
      isActive: item.isActive === 1 ? '启用' : '未启用',
      jobStatus: {
        'Open': '开放中',
        'Closed': '已关闭',
        'On Hold': '暂停中'
      }[item.jobStatus],
      positionType: {
        'Full-time': '全职',
        'Part-time': '兼职',
        'Intern': '实习'
      }[item.positionType]

    }));


    userList.value = processedRecords;
    total.value = res.total;
  });
};
/** 节点单击事件 */
function handleNodeClick(data) {
  queryParams.value.deptId = data.id;
  handleQuery();
};
/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
};
/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  proxy.resetForm("queryRef");
  queryParams.value.deptId = undefined;
  handleQuery();
  getList()
};

/** 批量删除按钮操作 */
function handleDeleteList() {
  const selectedRows = getSelectedRows(); // 获取选中的行数据
  if (!selectedRows || selectedRows.length === 0) {
    proxy.$modal.msgError("请选择要删除的数据项");
    return;
  }

  const enterpriseJobIds = selectedRows.map(row => row.enterpriseJobId);
  proxy.$modal.confirm(`是否确认删除标签编号为"${enterpriseJobIds}"的数据项？`).then(function () {
    return deleteJobEnterprisePositions(enterpriseJobIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
};
/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除招聘信息为"' + row.enterpriseJobId + '"的数据项？').then(function () {
    return delRecruitment(row.enterpriseJobId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
};
/** 导出按钮操作 */
function handleExport() {
  proxy.download("/api/job-enterprise-positions/export", {
    ...queryParams.value,
  },`job-enterprise-positions_${new Date().getTime()}.xlsx`);
};
/** 用户状态修改  */
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用";
  proxy.$modal.confirm('确认要"' + text + '""' + row.userName + '"用户吗?').then(function () {
    return changeUserStatus(row.userId, row.status);
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功");
  }).catch(function () {
    row.status = row.status === "0" ? "1" : "0";
  });
};
/** 更多操作 */
function handleCommand(command, row) {
  switch (command) {
    case "handleResetPwd":
      handleResetPwd(row);
      break;
    case "handleAuthRole":
      handleAuthRole(row);
      break;
    default:
      break;
  }
};
/** 跳转角色分配 */
function handleAuthRole(row) {
  const userId = row.userId;
  router.push("/system/user-auth/role/" + userId);
};
/** 重置密码按钮操作 */
function handleResetPwd(row) {
  proxy.$prompt('请输入"' + row.userName + '"的新密码', "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    closeOnClickModal: false,
    inputPattern: /^.{5,20}$/,
    inputErrorMessage: "用户密码长度必须介于 5 和 20 之间",
  }).then(({ value }) => {
    resetUserPwd(row.userId, value).then(response => {
      proxy.$modal.msgSuccess("修改成功，新密码是：" + value);
    });
  }).catch(() => {});
};
const multipleTable = ref(null);
const multipleSelection = ref([]);
/** 选择条数  */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.userId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  multipleSelection.value = selection; // 移除多余的点
}

// 获取选中的行数据
const getSelectedRows = () => {
  return multipleSelection.value;
};
/** 导入按钮操作 */
function handleImport() {
  upload.title = "用户导入";
  upload.open = true;
};
/** 下载模板操作 */
function importTemplate() {
  proxy.download("system/user/importTemplate", {
  }, `user_template_${new Date().getTime()}.xlsx`);
};
/**文件上传中处理 */
const handleFileUploadProgress = (event, file, fileList) => {
  upload.isUploading = true;
};
/** 文件上传成功处理 */
const handleFileSuccess = (response, file, fileList) => {
  upload.open = false;
  upload.isUploading = false;
  proxy.$refs["uploadRef"].handleRemove(file);
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
  getList();
};
/** 提交上传文件 */
function submitFileForm() {
  proxy.$refs["uploadRef"].submit();
};
/** 重置操作表单 */
function reset() {
  form.value = {
    enterpriseJobId: undefined,
    enterpriseId: undefined,
    enterpriseName: undefined,
    jobPosition: undefined,
    isActive: undefined,
    enterpriseJobName: undefined,
    location: undefined,
    positionType: "Full-time",
    jobName: "",
    jobDescription: "",
    jobStatus: "Open",
    jobRequirements: "",
    jobBenefits: "",
    enterpriseOptions: form.value.enterpriseOptions,
    positionOptions: form.value.positionOptions
  };
  proxy.resetForm("userRef");
};
/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
};
/** 新增按钮操作 */
function handleAdd() {
  reset();
  getUser().then(response => {
    postOptions.value = response.posts;
    roleOptions.value = response.roles;
    open.value = true;
    title.value = "添加招聘信息";
    form.value.password = initPassword.value;
  });
};
/** 修改按钮操作 */
function handleUpdate(row) {
  reset();

  if (row && row.enterpriseJobId) {
    getRecruitment(row.enterpriseJobId).then(response => {
      form.value = { ...response, enterpriseOptions: form.value.enterpriseOptions,positionOptions: form.value.positionOptions }
      open.value = true
      title.value = "修改招聘信息"
      form.value.isActive=form.value.isActive.toString()
      console.log("rowsssssssssss", form.value.isActive)
    }).catch(error => {
      console.error("修改招聘信息时出错：", error)
      proxy.$modal.msgError("修改招聘信息失败，请重试")
    })
  }
};
/** 提交按钮 */
function submitForm() {
  proxy.$refs["userRef"].validate(valid => {
    if (valid) {
      if (form.value.enterpriseJobId != undefined) {
        updateRecruitment(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addRecruitment(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
};

function getJobPositionOption() {
  getJobPositionOptions().then(res => {

    const positionNames = res.map(item => item.positionName)
    form.value.positionOptions = positionNames
    console.log("form.value.positionOptions", form.value.positionOptions)
  }).catch(error => {
    console.error(error)
  })
}

function getEnterpriseOption() {
  getEnterpriseOptions().then(res => {
    const companyNames = res.map(item => item.companyName)
    form.value.enterpriseOptions = companyNames
    console.log("form.value.enterpriseOptions", form.value.enterpriseOptions)
  }).catch(error => {
    console.error(error)
  })
}

onMounted(() => {

  getList()
  getEnterpriseOption()
  getJobPositionOption()
})
</script>

<style scoped>
.app-container {
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.full-width-table {
  width: 100%;
}

/* If you want to remove any default padding/margin */
.el-table {
  margin: 0;
  padding: 0;
}
</style>