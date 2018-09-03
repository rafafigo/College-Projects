def digit(n):
    if n == '':
        return False
    elif len(n) == 1:
        return ('0' <= n <= '9')
    else:
        return ('0' <= n[0] <= '9') and digit(n[1:])
print(digit('543+1')