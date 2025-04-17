export default {
  path: "/asr",
  redirect: "/error/403",
  meta: {
    icon: "ri:number-0",
    // showLink: false,
    title: "asr",
    rank: 9
  },
  children: [
    {
      path: "/asr/test",
      name: "test",
      component: () => import("@/views/asr/test.vue"),
      meta: {
        title: "test"
      }
    },{
      path: "/asr/sample",
      name: "sample",
      component: () => import("@/views/asr/sample.vue"),
      meta: {
        title: "sample"
      }
    },{
      path: "/asr/Sample2",
      name: "Sample2",
      component: () => import("@/views/asr/Sample2.vue"),
      meta: {
        title: "Sample2"
      }
    },{
      path: "/asr/play",
      name: "play",
      component: () => import("@/views/asr/play.vue"),
      meta: {
        title: "play"
      }
    },{
      path: "/asr/record",
      name: "record",
      component: () => import("@/views/asr/record.vue"),
      meta: {
        title: "record"
      }
    },{
      path: "/error/403",
      name: "403",
      component: () => import("@/views/error/403.vue"),
      meta: {
        title: "403"
      }
    },
  ]
};
// } satisfies RouteConfigsTable;
