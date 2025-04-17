// 模拟后端动态生成路由
import { defineFakeRoute } from "vite-plugin-fake-server/client";

/**
 * roles：页面级别权限，这里模拟二种 "admin"、"common"
 * admin：管理员角色
 * common：普通角色
 */
const permissionRouter = {
  path: "/permission",
  meta: {
    title: "权限管理",
    icon: "ep:lollipop",
    rank: 10
  },
  children: [
    {
      path: "/permission/page/index",
      name: "PermissionPage",
      meta: {
        title: "页面权限",
        roles: ["admin", "common"]
      }
    },
    {
      path: "/permission/button",
      meta: {
        title: "按钮权限",
        roles: ["admin", "common"]
      },
      children: [
        {
          path: "/permission/button/router",
          component: "permission/button/index",
          name: "PermissionButtonRouter",
          meta: {
            title: "路由返回按钮权限",
            auths: [
              "permission:btn:add",
              "permission:btn:edit",
              "permission:btn:delete"
            ]
          }
        },
        {
          path: "/permission/button/login",
          component: "permission/button/perms",
          name: "PermissionButtonLogin",
          meta: {
            title: "登录接口返回按钮权限"
          }
        }
      ]
    }
  ]
};

/*const seniorMiddleSchool = {
  path: "/asr2",
  meta: {
    icon: "ri:book-2-fill",
    showLink: true,
    title: "高中",
    rank: 92
  },
  children: [
    {
      path: "/asr2/obligatoryCourse1",
      query: { json: 'seniorMiddleSchool/obligatoryCourse1' },
      component: "asr2/content",
      name: "必修一",
      meta: {
        icon: "ri:booklet-fill",
        title: "必修一",
        showParent: true
      }
    },
    {
      path: "/asr2/obligatoryCourse2",
      query: { json: 'seniorMiddleSchool/obligatoryCourse2' },
      component: "asr2/content",
      name: "必修二",
      meta: {
        title: "必修二",
        showParent: true
      }
    },
  ]
};*/

export default defineFakeRoute([
  {
    url: "/get-async-routes",
    method: "get",
    response: () => {
      return {
        success: true,
        data: []
      };
    }
  }
]);
