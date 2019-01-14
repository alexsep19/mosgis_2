define ([], function () {

    $_DO.update_planned_examinations_new = function (e) {

        var form = w2ui ['planned_examination_form']

        var v = form.values ()
        var shouldberegistered = clone ($('body').data ('data')).item.shouldberegistered

        var test_num = /^\d{0,3}$/
        var test_reg_num = /^\d{14}$/
        var date_min = new Date (1992, 1, 1)
        var date_max = new Date (2030, 12, 31)

        if (!v.numberinplan) die ('numberinplan', 'Пожалуйста, укажите номер проверки')
        else if (!test_num.test (v.numberinplan)) die ('numberinplan', 'Указан неверный номер проверки')

        if (shouldberegistered) {
            if (v.uriregistrationnumber == null) die ('uriregistrationnumber', 'Пожалуйста, укажите регистрационный номер')
            if (!test_reg_num.test (v.uriregistrationnumber)) die ('uriregistrationnumber', 'Указан неверный регистрационный номер')

            if (v.uriregistrationdate == null) die ('uriregistrationdate', 'Пожалуйста, укажите дату регистрации')
            if (v.uriregistrationdate < date_min || v.uriregistrationdate > date_max) die ('uriregistrationdate', 'Указана неверная дата регистрации')
        }

        if (!v.code_vc_nsi_65) die ('code_vc_nsi_65', 'Пожалуйста, укажите вид осуществления контрольной деятельности')
        if (!v.code_vc_nsi_71) die ('code_vc_nsi_71', 'Пожалуйста, укажите форму проведения проверки')

        v.check_plan_uuid = $_REQUEST.id
        
        var tia = {type: 'planned_examinations'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['check_plan_examinations_grid']

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу проверки?').yes (function () {openTab ('/planned_examination/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})