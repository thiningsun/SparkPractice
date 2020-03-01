
-- 创建表
-- 雇员表EMP
create table IF NOT EXISTS db_hive.emp(
empno int,
ename string,
job string,
mgr int,
hiredate string,
sal double,
comm double,
deptno int
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';

-- 部门表DEPT
create table IF NOT EXISTS db_hive.dept(
deptno int,
dname string,
loc string
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';

-- 加载数据至表中
load data local inpath '/opt/datas/emp.txt' overwrite into table emp ;
load data local inpath '/opt/datas/dept.txt' overwrite into table dept ;