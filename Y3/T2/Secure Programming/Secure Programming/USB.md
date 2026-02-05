### Format USB ( Window )

- **Open CMD as Administration** 
- Enter Disk Part tool with
```cmd
diskpart
```

- List Disk Options
```cmd
list disk
```

- Select Disk To Format 
```cmd
select disk x
```
	Replace x with disk number like Example : select disk 1

- Clean
```cmd
clean
```

- Create Partition 
```cmd
create partition primary
```

- Format into exFat
```cmd
format fs=exfat quick label="MYUSB"
```
	Can Replace MYUSB With Something Else

- Assign Letter
```cmd
assign letter=E
```
	Can assign other letter too :D

- Exit
```cmd
exit
```
