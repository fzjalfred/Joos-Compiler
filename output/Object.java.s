extern __malloc
extern NATIVEjava.io.OutputStream.nativeWrite
global _start
_start:
call test
mov ebx, eax
mov eax, 1
int 0x80
global Object_739498517
Object_739498517:
push ebp
mov ebp,esp
sub esp,4
mov ecx,[ ebp+8 ]
mov [ ebp-4 ],ecx
mov esp,ebp
pop ebp
ret
global equals_125130493
equals_125130493:
push ebp
mov ebp,esp
sub esp,8
mov ecx,[ ebp+8 ]
mov [ ebp-8 ],ecx
mov ecx,[ ebp+12 ]
mov [ ebp-4 ],ecx
mov esp,ebp
pop ebp
ret
global toString_914504136
toString_914504136:
push ebp
mov ebp,esp
sub esp,4
mov ecx,[ ebp+8 ]
mov [ ebp-4 ],ecx
mov esp,ebp
pop ebp
ret
global hashCode_166239592
hashCode_166239592:
push ebp
mov ebp,esp
sub esp,4
mov ecx,[ ebp+8 ]
mov [ ebp-4 ],ecx
mov esp,ebp
pop ebp
ret
global clone_991505714
clone_991505714:
push ebp
mov ebp,esp
sub esp,4
mov ecx,[ ebp+8 ]
mov [ ebp-4 ],ecx
mov esp,ebp
pop ebp
ret
global getClass_385242642
getClass_385242642:
push ebp
mov ebp,esp
sub esp,4
mov ecx,[ ebp+8 ]
mov [ ebp-4 ],ecx
mov esp,ebp
pop ebp
ret

