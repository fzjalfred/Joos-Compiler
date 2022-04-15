extern __malloc
extern NATIVEjava.io.OutputStream.nativeWrite
global _start
_start:
call test
mov ebx, eax
mov eax, 1
int 0x80
global Object_1297685781
Object_1297685781:
push ebp
mov ebp,esp
sub esp,4
mov ecx,[ ebp+8 ]
mov [ ebp-4 ],ecx
mov esp,ebp
pop ebp
ret
global equals_1705929636
equals_1705929636:
push ebp
mov ebp,esp
sub esp,16
mov ecx,[ ebp+8 ]
mov [ ebp-16 ],ecx
mov ecx,[ ebp+12 ]
mov [ ebp-4 ],ecx
mov edx,[ ebp-16 ]
mov [ ebp-12 ],edx
mov ecx,[ ebp-8 ]
mov edx,[ ebp-8 ]
xor ecx,edx
mov [ ebp-8 ],ecx
mov ecx,[ ebp-12 ]
mov edx,[ ebp-4 ]
cmp ecx,edx
mov [ ebp-12 ],ecx
sete cl
mov [ ebp-8 ],ecx
mov edx,[ ebp-8 ]
mov eax,edx
mov esp,ebp
pop ebp
ret
global toString_1221555852
toString_1221555852:
push ebp
mov ebp,esp
sub esp,4
mov ecx,[ ebp+8 ]
mov [ ebp-4 ],ecx
mov esp,ebp
pop ebp
ret
global hashCode_1509514333
hashCode_1509514333:
push ebp
mov ebp,esp
sub esp,4
mov ecx,[ ebp+8 ]
mov [ ebp-4 ],ecx
mov eax,42
mov esp,ebp
pop ebp
ret
global clone_1556956098
clone_1556956098:
push ebp
mov ebp,esp
sub esp,4
mov ecx,[ ebp+8 ]
mov [ ebp-4 ],ecx
mov edx,[ ebp-4 ]
mov eax,edx
mov esp,ebp
pop ebp
ret
global getClass_1252585652
getClass_1252585652:
push ebp
mov ebp,esp
sub esp,4
mov ecx,[ ebp+8 ]
mov [ ebp-4 ],ecx
mov esp,ebp
pop ebp
ret


