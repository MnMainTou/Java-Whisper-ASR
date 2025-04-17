const filePath = '/seniorMiddleSchool'; //音频和json文件存放的路径

const obligatoryCourse1UnitNumber = 5;
const obligatoryCourse1Children = [];

for (let i=0; i<=obligatoryCourse1UnitNumber;i++) {
  let name = '';
  if(i==0) {
    name = 'unit-welcome';
  }else {
    name = 'unit-'+i;
  }
  let jsonPath = filePath+'/obligatoryCourse1/'+name;
  obligatoryCourse1Children.push({
    path: jsonPath,
    query: { json: jsonPath, fileName: name },
    name: "必修一 "+name,
    component: () => import("@/views/asr2/content.vue"),
    meta: {
      title: name,
      showParent: true
    },
  });
}

const obligatoryCourse2UnitNumber = 5;
const obligatoryCourse2Children = [];
const obligatoryCourse3Children = [];
for (let i=1; i<=obligatoryCourse2UnitNumber;i++) {
  let name = '';
  if(i==0) {
    name = 'unit-welcome';
  }else {
    name = 'unit-'+i;
  }
  let jsonPath = filePath+'/obligatoryCourse2/'+name;
  let jsonPath3 = filePath+'/obligatoryCourse2/'+name;
  obligatoryCourse2Children.push({
    path: jsonPath,
    query: { json: jsonPath, fileName: name },
    name: "必修一 "+name,
    component: () => import("@/views/asr2/content.vue"),
    meta: {
      title: name,
      showParent: true
    },
  });
  obligatoryCourse3Children.push({
    path: jsonPath,
    query: { json: jsonPath, fileName: name },
    name: "必修一 "+name,
    component: () => import("@/views/asr2/content.vue"),
    meta: {
      title: name,
      showParent: true
    },
  })
}

export default {
  path: "/seniorMiddle",
  redirect: "/seniorMiddle/obligatoryCourse1",
  meta: {
    icon: "ri:book-2-fill",
    title: "高中",
    rank: 9
  },
  children: [
    {
      path: "/seniorMiddle/obligatoryCourse1",
      meta: {
        icon: "ri:book-open-fill",
        title: "必修一",
        showParent: true
      },
      children: obligatoryCourse1Children
    },
    {
      path: "/seniorMiddle/obligatoryCourse2",
      name: "必修二",
      component: () => import("@/views/asr2/content.vue"),
      meta: {
        icon: "ri:book-open-fill",
        title: "必修二",
        showParent: true
      },
      children: obligatoryCourse2Children
    },
    {
      path: "/seniorMiddle/obligatoryCourse3",
      name: "必修三",
      component: () => import("@/views/asr2/content.vue"),
      meta: {
        icon: "ri:book-open-fill",
        title: "必修三",
        showParent: true
      },
      children: obligatoryCourse3Children
    },
  ]
} satisfies RouteConfigsTable;
