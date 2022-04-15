extern Object_1297685781
global classTest_1761061602
classTest_1761061602:
push ebp
mov ebp,esp
sub esp,16
mov ecx,Object_1297685781
mov [ ebp-8 ],ecx
mov edx,[ ebp-16 ]
mov [ ebp-12 ],edx
mov ecx,[ ebp-12 ]
push ecx
mov ecx,[ ebp-8 ]
call ecx
add esp,4
mov ecx,[ ebp+8 ]
mov [ ebp-16 ],ecx
mov esp,ebp
pop ebp
ret

