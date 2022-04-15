extern classTest_1761061602
extern hashCode_1509514333
extern getClass_1252585652
extern clone_1556956098
extern equals_1705929636
extern toString_1221555852
global classTestA_59559151
classTestA_59559151:
push ebp
mov ebp,esp
sub esp,16
mov ecx,classTest_1761061602
mov [ ebp-16 ],ecx
mov edx,[ ebp-12 ]
mov [ ebp-8 ],edx
mov ecx,[ ebp-8 ]
push ecx
mov ecx,[ ebp-16 ]
call ecx
add esp,4
mov ecx,[ ebp+8 ]
mov [ ebp-12 ],ecx
mov esp,ebp
pop ebp
ret
global test
test:
push ebp
mov ebp,esp
sub esp,140
mov ecx,8
mov [ ebp-8 ],ecx
mov edx,[ ebp-8 ]
mov eax,edx
call __malloc
mov ecx,eax
mov [ ebp-140 ],ecx
mov edx,[ ebp-140 ]
mov [ ebp-92 ],edx
mov edx,[ ebp-92 ]
lea ecx,[ edx+4 ]
mov [ ebp-116 ],ecx
mov edx,[ ebp-116 ]
mov [ ebp-84 ],edx
mov edx,[ ebp-84 ]
mov word [ edx ],3
mov ecx,24
mov [ ebp-48 ],ecx
mov edx,[ ebp-48 ]
mov eax,edx
call __malloc
mov ecx,eax
mov [ ebp-12 ],ecx
mov edx,[ ebp-140 ]
mov [ ebp-60 ],edx
mov edx,[ ebp-60 ]
mov ecx,[ ebp-12 ]
mov [ edx ],ecx
mov edx,[ ebp-140 ]
mov ecx,[ edx ]
mov [ ebp-132 ],ecx
mov ecx,toString_1221555852
mov [ ebp-124 ],ecx
mov edx,[ ebp-132 ]
mov [ ebp-4 ],edx
mov edx,[ ebp-4 ]
lea ecx,[ edx+20 ]
mov [ ebp-108 ],ecx
mov edx,[ ebp-108 ]
mov [ ebp-100 ],edx
mov edx,[ ebp-100 ]
mov ecx,[ ebp-124 ]
mov [ edx ],ecx
mov ecx,hashCode_1509514333
mov [ ebp-124 ],ecx
mov edx,[ ebp-132 ]
mov [ ebp-64 ],edx
mov edx,[ ebp-64 ]
lea ecx,[ edx+8 ]
mov [ ebp-24 ],ecx
mov edx,[ ebp-24 ]
mov [ ebp-88 ],edx
mov edx,[ ebp-88 ]
mov ecx,[ ebp-124 ]
mov [ edx ],ecx
mov ecx,equals_1705929636
mov [ ebp-124 ],ecx
mov edx,[ ebp-132 ]
mov [ ebp-36 ],edx
mov edx,[ ebp-36 ]
lea ecx,[ edx+12 ]
mov [ ebp-20 ],ecx
mov edx,[ ebp-20 ]
mov [ ebp-32 ],edx
mov edx,[ ebp-32 ]
mov ecx,[ ebp-124 ]
mov [ edx ],ecx
mov ecx,getClass_1252585652
mov [ ebp-124 ],ecx
mov edx,[ ebp-132 ]
mov [ ebp-40 ],edx
mov edx,[ ebp-40 ]
lea ecx,[ edx+4 ]
mov [ ebp-16 ],ecx
mov edx,[ ebp-16 ]
mov [ ebp-56 ],edx
mov edx,[ ebp-56 ]
mov ecx,[ ebp-124 ]
mov [ edx ],ecx
mov ecx,clone_1556956098
mov [ ebp-124 ],ecx
mov edx,[ ebp-132 ]
mov [ ebp-28 ],edx
mov edx,[ ebp-28 ]
lea ecx,[ edx+16 ]
mov [ ebp-52 ],ecx
mov edx,[ ebp-52 ]
mov [ ebp-76 ],edx
mov edx,[ ebp-76 ]
mov ecx,[ ebp-124 ]
mov [ edx ],ecx
mov ecx,classTestA_59559151
mov [ ebp-104 ],ecx
mov edx,[ ebp-140 ]
mov [ ebp-128 ],edx
mov ecx,[ ebp-128 ]
push ecx
mov ecx,[ ebp-104 ]
call ecx
add esp,4
mov edx,[ ebp-140 ]
mov [ ebp-80 ],edx
mov edx,[ ebp-80 ]
mov [ ebp-136 ],edx
mov edx,[ ebp-136 ]
mov [ ebp-96 ],edx
mov edx,[ ebp-96 ]
lea ecx,[ edx+4 ]
mov [ ebp-72 ],ecx
mov edx,[ ebp-72 ]
mov [ ebp-136 ],edx
mov edx,[ ebp-136 ]
mov ecx,[ edx ]
mov [ ebp-68 ],ecx
mov edx,[ ebp-68 ]
mov eax,edx
mov esp,ebp
pop ebp
ret

