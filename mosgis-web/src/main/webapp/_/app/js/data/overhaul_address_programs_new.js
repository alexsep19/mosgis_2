define ([], function () {

    $_DO.update_overhaul_address_programs_new = function (e) {

        var form = w2ui ['overhaul_address_programs_new_form']

        var v = form.values ()

        var reg_year = /^[1-9][0-9]{3}$/
        var reg_month = /^[1-9][0-2]{0,1}$/

        if (!v.programname) die ('programname', 'Укажите, пожалуйста, наименование адресной программы')
        
        if (!v.startmonth) die ('startmonth', 'Укажите, пожалуйста, месяц начала')
        if (!reg_month.test (v.startmonth)) die ('startmonth', 'Укажите, пожалуйста, корректное значение месяца начала')
            if (v.startmonth > 12) die ('startmonth', 'Укажите, пожалуйста, корректное значение месяца начала')
        if (!v.startyear) die ('startyear', 'Укажите, пожалуйста, год начала')
        if (!reg_year.test (v.startyear)) die ('startyear', 'Укажите, пожалуйста, корректное значение года начала')

        if (!v.endmonth) die ('endmonth', 'Укажите, пожалуйста, месяц окончания')
        if (!reg_month.test (v.endmonth)) die ('endmonth', 'Укажите, пожалуйста, корректное значение месяца окончания')
            if (v.endmonth > 12) die ('endmonth', 'Укажите, пожалуйста, корректное значение месяца окончания')
        if (!v.endyear) die ('endyear', 'Укажите, пожалуйста, год окончания')
        if (!reg_year.test (v.endyear)) die ('endyear', 'Укажите, пожалуйста, корректное значение года окончания')

        if (v.endyear < v.startyear) die ('endyear', 'Неверный временной промежуток')
        else if (v.endyear == v.startyear && v.endmonth <= v.startmonth) die ('endmonth', 'Неверный временной промежуток')

        if (v.endyear - v.startyear > 3) die ('endyear', 'Период реализации адресной прораммы не должен превышать 3-х лет')

        if (!$_USER.role.admin) v.org_uuid = $_USER.uuid_org
        
        var tia = {type: 'overhaul_address_programs'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['overhaul_address_programs_grid']
        
        var data = clone ($('body').data ('data'))

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу адресной программы?').yes (function () {openTab ('/overhaul_address_program/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})