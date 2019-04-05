define ([], function () {

    $_DO.update_overhaul_regional_programs_new = function (e) {

        var form = w2ui ['overhaul_regional_programs_new_form']

        var v = form.values ()

        var reg_year = /^[1-9][0-9]{3}$/
        
        if (!v.startyear) die ('startyear', 'Пожалуйста, укажите год начала периода реализации')
        if (!reg_year.test (v.startyear)) die ('startyear', 'Пожалуйста, укажите корректное значение года начала периода реализации')
        if (!v.endyear) die ('endyear', 'Пожалуйста, укажите год окончания периода реализации')
        if (!reg_year.test (v.endyear)) die ('endyear', 'Пожалуйста, укажите корректное значение года окончания периода реализации')
        if (v.endyear < v.startyear) die ('endyear', 'Год окончания реализации не может быть раньше года начала реализации')
        if (v.startyear < 2005) die ('startyear', 'Год начала реализации не может быть раньше 2005 года')
        if (v.endyear > 2060) die ('endyear', 'Год окончания реализации не может быть позже 2060 года')
        if (!v.programname) die ('programname', 'Пожалуйста, укажите наименование')

        if (!$_USER.role.admin) v.org_uuid = $_USER.uuid_org
        
        var tia = {type: 'overhaul_regional_programs'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['overhaul_regional_programs_grid']
        
        var data = clone ($('body').data ('data'))

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу региональной программы?').yes (function () {openTab ('/overhaul_regional_program/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})