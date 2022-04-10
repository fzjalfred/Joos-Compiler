extern NATIVEjava.io.OutputStream.nativeWrite

global _start
_start:
call test
mov ebx, eax
mov eax, 1
int 0x80
global bar_1330106945
bar_1330106945:
push ebp
mov ebp,esp
sub esp,12
mov ecx,[ ebp+8 ]
mov [ ebp-4 ],ecx
mov edx,[ ebp-4 ]
mov [ ebp-12 ],edx
mov ecx,[ ebp-12 ]
lea ecx,[ edx+3 ]
mov [ ebp-8 ],ecx
mov edx,[ ebp-8 ]
mov eax,edx
mov esp,ebp
pop ebp
ret
global test
test:
push ebp
mov ebp,esp
sub esp,28
mov ecx,3
mov [ ebp-20 ],ecx
mov ecx,bar_1330106945
mov [ ebp-28 ],ecx
mov ecx,3
mov [ ebp-16 ],ecx
mov ecx,[ ebp-16 ]
push ecx
mov ecx,[ ebp-28 ]
call ecx
mov ecx,eax
mov [ ebp-12 ],ecx
add esp,4
mov edx,[ ebp-12 ]
mov [ ebp-4 ],edx
mov ecx,6
mov [ ebp-24 ],ecx
mov ecx,[ ebp-4 ]
mov edx,[ ebp-24 ]
cmp ecx,edx
mov [ ebp-4 ],ecx
je true_735937428
jmp false_735937428
true_735937428:
mov eax, 't'
call NATIVEjava.io.OutputStream.nativeWrite
false_735937428:
mov eax, 'f'
call NATIVEjava.io.OutputStream.nativeWrite
res:
mov eax,0
mov esp,ebp
pop ebp
ret

