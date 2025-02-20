<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--用户数据-->
      <el-col :span="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
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
          <el-table-column label="申请类型" align="center" key="applicationType" prop="applicationType" v-if="columns[1].visible" :show-overflow-tooltip="true" />
          <el-table-column label="流程步骤顺序" align="center" key="stepOrder" prop="stepOrder" v-if="columns[2].visible" :show-overflow-tooltip="true" />
          <el-table-column label="部门" align="center" key="department" prop="department" v-if="columns[3].visible" :show-overflow-tooltip="true" />
          <el-table-column label="角色" align="center" key="role" prop="role" v-if="columns[4].visible" width="120" />
          <el-table-column label="创建时间" align="center" key="createdAt" prop="createdAt" v-if="columns[2].visible" :show-overflow-tooltip="true" >
            <template #default="scope">
              <span>{{ parseTime(scope.row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="更新时间" align="center" key="updatedAt" prop="updatedAt" v-if="columns[3].visible" :show-overflow-tooltip="true" >
            <template #default="scope">
              <span>{{ parseTime(scope.row.updatedAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-tooltip content="修改" placement="top" v-if="scope.row.userId !== 1">
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:user:edit']"></el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top" v-if="scope.row.userId !== 1">
                <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:user:remove']"></el-button>
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
      <el-form :model="form" :rules="rules" ref="userRef" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="申请类型" prop="applicationType">
              <el-select v-model="form.applicationType" placeholder="请选择申请类型">
                <el-option
                    v-for="option in form.applicationTypeOptions"
                    :key="option"
                    :label="option"
                    :value="option"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="流程顺序" prop="stepOrder">
              <el-input v-model="form.stepOrder" placeholder="请输入流程顺序" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="部门" prop="department">
              <el-select v-model="form.department" placeholder="请选择部门">
                <el-option
                    v-for="option in form.departmentOptions"
                    :key="option"
                    :label="option"
                    :value="option"
                />
              </el-select>
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



  </div>
</template>

<script setup name="User">

import {  getUser } from "@/api/system/user";

import {
  addApprovalProcess, deleteApprovalProcess, deleteApprovalProcessList, getApplicationTypeOptions,
  getApprovalProcessById,
  getApprovalProcessPage, optionselect1, selectDeptList,
  updateApprovalProcess
} from "../../../api/approval/applyProcess";

import {onMounted, ref} from "vue";

const router = useRouter();
const { proxy } = getCurrentInstance();


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

// 列显隐信息
const columns = ref([
  { key: 0, label: `用户编号`, visible: true },
  { key: 1, label: `申请人姓名`, visible: true },
  { key: 2, label: `申请人手机号`, visible: true },
  { key: 3, label: `申请人邮箱`, visible: true },
  { key: 4, label: `申请日期`, visible: true },
  { key: 5, label: `申请状态`, visible: true },
  { key: 6, label: `申请日期`, visible: true },
  { key: 7, label: `企业描述`, visible: true }
]);

const data = reactive({
  form: {
    processId: null,
    applicationType: null,
    applicationTypeId: null,
    stepOrder: null,
    department: null,
    role: null,
    departmentId: null,
    roleId: null,
    createdAt: null,
    updatedAt: null,
    applicationTypeOptions: [],
    departmentOptions: [],
    roleOptions: [],
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
    sequence: [{ required: true, message: '请输入流程顺序', trigger: 'blur' }],
    approvalStatus: [{ required: true, message: '请选择审批状态', trigger: 'change' }],
    comments: [{ required: true, message: '请输入审批备注', trigger: 'blur' }],
    stepOrder: [
      { required: true, message: '请输入流程顺序', trigger: 'blur' },
      { validator: validateStepOrder, trigger: 'blur' }
    ]
  },

});
const approvalStatusOptions = [
  { value: 0, label: '拒绝' },
  { value: 1, label: '通过' },
  { value: 2, label: '待定' }
];
const { queryParams, form, rules } = toRefs(data);
function validateStepOrder(rule, value, callback) {
  if (value === '' || value === null) {
    callback(new Error('请输入流程顺序'));
  } else {
    const num = Number(value);
    if (isNaN(num)) {
      callback(new Error('流程顺序必须是数字'));
    } else if (!Number.isInteger(num)) {
      callback(new Error('流程顺序必须是整数'));
    } else if (num < 1) {
      callback(new Error('流程顺序必须大于0'));
    } else {
      callback();
    }
  }
}

/** 查询用户列表 */
// 定义审批状态的映射对象
const approvalStatusMap = {
  0: '拒绝',
  1: '通过',
  2: '待定'
};

function getList() {
  loading.value = true;
  getApprovalProcessPage(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {

    // 遍历记录，将审批状态转换为中文
    const transformedRecords = res.records.map(record => ({
      ...record,
      approvalStatus: approvalStatusMap[record.approvalStatus] || '未知状态'
    }));

    loading.value = false;
    userList.value = transformedRecords;
    total.value = res.total;
  });
}


/** 批量删除按钮操作 */
function handleDeleteList() {
  const selectedRows = getSelectedRows(); // 获取选中的行数据
  if (!selectedRows || selectedRows.length === 0) {
    proxy.$modal.msgError("请选择要删除的数据项");
    return;
  }

  const processIds = selectedRows.map(row => row.processId);
  proxy.$modal.confirm(`是否确认删除标签编号为"${processIds}"的数据项？`).then(function () {
    return deleteApprovalProcessList(processIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
};
/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除用户编号为"' + row.processId + '"的数据项？').then(function () {
    return deleteApprovalProcess(row.processId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
};
/** 导出按钮操作 */
function handleExport() {
  proxy.download("/api/approval-process/export", {
    ...queryParams.value,
  },`approval-process_${new Date().getTime()}.xlsx`);
};


const multipleTable = ref(null);
const multipleSelection = ref([]);


// 获取选中的行数据
const getSelectedRows = () => {
  return multipleSelection.value;
};


/** 重置操作表单 */
function reset() {
  form.value = {
    processId: null,
    applicationType: null,
    applicationTypeId: null,
    stepOrder: null,
    department: null,
    role: null,
    departmentId: null,
    roleId: null,
    createdAt: null,
    updatedAt: null,
    applicationTypeOptions: form.value.applicationTypeOptions,
    departmentOptions: form.value.departmentOptions,
    roleOptions: form.value.roleOptions,
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
    title.value = "添加审批流程";
    form.value.password = initPassword.value;
  });
};
/** 修改按钮操作 */

function handleUpdate(row) {
  reset();

  if (row && row.processId) {
    getApprovalProcessById(row.processId).then(response => {
      form.value = { ...response,      applicationTypeOptions: form.value.applicationTypeOptions,
        departmentOptions: form.value.departmentOptions,
        roleOptions: form.value.roleOptions,}
      console.log("form.value", form.value)
      open.value = true
      title.value = "修改审批流程"
    }).catch(error => {
      console.error("修改审批流程时出错：", error)
      proxy.$modal.msgError("修改审批流程失败，请重试")
    })
  }
};
/** 提交按钮 */
const submitForm = () => {
  proxy.$refs["userRef"].validate(valid => {
    if (valid) {
      if (form.value.processId != undefined) {
        updateApprovalProcess(form.value)
            .then(response => {
              proxy.$modal.msgSuccess("修改成功");
              open.value = false;
              getList();
            })
            .catch(error => {
              // 只在控制台记录错误
              console.warn("修改审批流程时出错:", error);
              // 显示友好的错误提示
              proxy.$modal.msgError("操作失败，请稍后再试。");
            });
      } else {
        addApprovalProcess(form.value)
            .then(response => {
              console.log("response ", response);
              proxy.$modal.msgSuccess("新增成功");
              open.value = false;
              getList();
            })
            .catch(error => {
              // 在控制台记录详细错误
              console.warn("新增审批流程时出错:", error);

              // 检查是否为数字格式错误，但只在控制台显示详细信息
              if (error.message && error.message.includes('Cannot deserialize value of type `int` from String')) {
                console.warn("数字格式错误：", error.message);
                proxy.$modal.msgError("请输入有效的数字格式");
              } else {
                // 对于其他错误，显示通用错误消息
                proxy.$modal.msgError("操作失败，请稍后再试。");
              }
            });
      }
    }
  });
}




function getApplicationTypeOptions1() {
  getApplicationTypeOptions().then(res => {
    const applicationNames = res.map(item => item.applicationName)
    form.value.applicationTypeOptions = applicationNames
    console.log("form.value.applicationTypeOptions", form.value.applicationTypeOptions)
  }).catch(error => {
    console.error(error)
  })
}


function optionselect() {
  optionselect1().then(res => {
    const roleNames = res.map(item => item.roleName)
    form.value.roleOptions = roleNames
    console.log("form.value.roleOptions", form.value.roleOptions)
  }).catch(error => {
    console.error(error)
  })
}

onMounted(() => {

  getList()

  optionselect()
  getApplicationTypeOptions1()

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