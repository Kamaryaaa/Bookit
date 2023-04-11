select firstname,lastname,role from users
where email ='raymond@cydeo.com';

select location,batch_number,name from team join campus c on team.campus_id = c.id
where name='Gryffindor123';