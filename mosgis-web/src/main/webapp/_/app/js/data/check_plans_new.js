define ([], function () {

    $_DO.update_check_plan_new = function (e) {

        var form = w2ui ['check_plan_form']

        var v = form.values ()
        
        if (!v.year) die ('year', 'Пожалуйста, укажите год')
        if (!v.hasOwnProperty ('shouldberegistered')) die ('shouldberegistered', 'Пожалуйста, укажите, должен ли план быть зарегистрированным в ЕРП')
        if (v.shouldberegistered && !v.uriregistrationplannumber) die ('uriregistrationplannumber', 'Пожалуйста, укажите регистрационный номер плана')

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
        
        done (data)

    }

})