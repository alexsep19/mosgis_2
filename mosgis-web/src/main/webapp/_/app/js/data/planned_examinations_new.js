define ([], function () {

    $_DO.update_check_plans_new = function (e) {

        var form = w2ui ['check_plan_form']

        var v = form.values ()
        var re = /^\d{1,12}$/
        
        if (!v.year) die ('year', 'Пожалуйста, укажите год')
        if (v.year < 1992 || v.year > 2030) die ('year', 'Пожалуйста, укажите корректное значение года (1992 - 2030)')
        if (!v.hasOwnProperty ('shouldberegistered')) die ('shouldberegistered', 'Пожалуйста, укажите, должен ли план быть зарегистрированным в ЕРП')
        if (v.shouldberegistered) {

            if (v.uriregistrationplannumber == null) die ('uriregistrationplannumber', 'Пожалуйста, укажите регистрационный номер')
            if (!re.test (v.uriregistrationplannumber)) die ('uriregistrationplannumber', 'Указан неверный регистрационный номер')

        }

        v.sign = 0
        v.shouldnotberegistered = 1 - v.shouldberegistered
        delete v.shouldberegistered
        
        var tia = {type: 'check_plans'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['check_plans_grid']

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу плана проверки?').yes (function () {openTab ('/check_plan/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ())
        
        data.record = $_SESSION.delete ('record')
        
        query ({type: 'planned_examinations', part: 'vocs', id: undefined}, {}, function (d) {

            add_vocabularies (d, d)

            data['vocs'] = d

            $('body').data ('data', data)

            done (data)

        }) 

    }

})