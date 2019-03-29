define ([], function () {

    $_DO.update_overhaul_regional_program_house_works_popup = function (e) {

        var f = w2ui ['overhaul_regional_program_house_works_popup_form']

        var v = f.values ()

        var reg_year = /^[1-9][0-9]{3}$/
        var reg_month = /^[1][0-2]{0,1}$/
        
        if (!v.work) die ('work', 'Укажите, пожалуйста, вид работ')
        
        if (!v.startmonth) die ('startmonth', 'Укажите, пожалуйста, месяц начала')
        if (!reg_month.test (v.startmonth)) die ('startmonth', 'Укажите, пожалуйста, корректное значение месяца начала')
        if (!v.startyear) die ('startyear', 'Укажите, пожалуйста, год начала')
        if (!reg_year.test (v.startyear)) die ('startyear', 'Укажите, пожалуйста, корректное значение года начала')

        if (!v.endmonth) die ('endmonth', 'Укажите, пожалуйста, месяц окончания')
        if (!reg_month.test (v.endmonth)) die ('endmonth', 'Укажите, пожалуйста, корректное значение месяца окончания')
        if (!v.endyear) die ('endyear', 'Укажите, пожалуйста, год окончания')
        if (!reg_year.test (v.endyear)) die ('endyear', 'Укажите, пожалуйста, корректное значение года окончания')

        v.house_uuid = $_REQUEST.id

        var tia = {type: 'overhaul_regional_program_house_works'}
        tia.id = f.record.id
        tia.action = tia.id ? 'update' : 'create'

        query (tia, {data: v}, reload_page)
            
    }

    return function (done) {
        
        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete ('record')
    
        done (data)
        
    }
    
})