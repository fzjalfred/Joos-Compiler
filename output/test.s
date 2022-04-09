global _start
_start:
call test
mov ebx, eax
mov eax, 1
int 0x80
global test
test:
push ebp
mov ebp,esp
sub esp,16
mov ecx,3
mov [ ebp-16 ],ecx
mov edx,[ ebp-16 ]
mov [ ebp-12 ],edx
mov ecx,1
mov [ ebp-4 ],ecx
mov ecx,[ ebp-12 ]
mov edx,[ ebp-4 ]
cmp ecx,edx
mov [ ebp-12 ],ecx
jl condAndlabel_271095942
jmp false_613784740
condAndlabel_271095942:
jmp true_613784740
true_613784740:
mov ecx,1
mov [ ebp-8 ],ecx
false_613784740:
mov eax,0
mov esp,ebp
pop ebp
ret

