define ([], function () {

    $_DO.update_overhaul_address_program_house_works_popup = function (e) {

        var data = clone ($('body').data ('data'))

        var f = w2ui ['overhaul_address_program_house_works_popup_form']

        var v = f.values ()

        var reg_year = /^[1-9][0-9]{3}$/
        var reg_month = /^[1-9][0-2]{0,1}$/
        var reg_cost = /^[0-9]{1,12}([.][0-9]{1,2})?$/

        var cost_max = 999999999999.99
        
        if (!v.work) die ('work', 'Укажите, пожалуйста, вид работ')

        if (!v.endyear) die ('endyear', 'Укажите, пожалуйста, год окончания')
        if (!reg_year.test (v.endyear)) die ('endyear', 'Укажите, пожалуйста, корректное значение года окончания')

        if (!v.endmonth) die ('endmonth', 'Укажите, пожалуйста, месяц окончания')
        if (!reg_month.test (v.endmonth)) die ('endmonth', 'Укажите, пожалуйста, корректное значение месяца окончания')
        if (v.endmonth > 12) die ('endmonth', 'Укажите, пожалуйста, корректное значение месяца окончания')

        if (v.endyear > data.item.endyear) die ('endyear', 'Указанный год окончания позже года окончания КПР')
        if (v.endyear == data.item.endyear && v.endmonth > data.item.endmonth) die ('endmonth', 'Указанный месяц окончания позже месяца окончания КПР')

        if (v.endyear < data.item.startyear) die ('endyear', 'Указанный год окончания раньше года начала КПР')
        if (v.endyear == data.item.startyear && v.endmonth < data.item.endmonth) die ('endmonth', 'Указанный месяца окончания раньше месяца начала КПР')

        if (!v.fund) v.fund = 0
            else if (v.fund < 0 || v.fund > cost_max || !reg_cost.test (v.fund)) die ('fund', 'Укажите, пожалуйста, корректное значение финансирования из Фонда ЖКХ')
        if (!v.regionbudget) v.regionbudget = 0
            else if (v.regionbudget < 0 || v.regionbudget > cost_max || !reg_cost.test (v.regionbudget)) die ('regionalbudget', 'Укажите, пожалуйста, корректное значение финансирования из бюджета субъекта РФ')
        if (!v.municipalbudget) v.municipalbudget = 0
            else if (v.municipalbudget < 0 || v.municipalbudget > cost_max || !reg_cost.test (v.municipalbudget)) die ('municipalbudget', 'Укажите, пожалуйста, корректное значение финансирования из местного бюджета')
        if (!v.owners) v.owners = 0
            else if (v.owners < 0 || v.owners > cost_max || !reg_cost.test (v.owners)) die ('owners', 'Укажите, пожалуйста, корректное значение финансирования из средств собственников')

        if (!v.specificcost || !reg_cost.test (v.specificcost) || v.specificcost == 0) die ('specificcost', 'Укажите, пожалуйста, корректное значение удельной стоимости работы')
        if (!v.maximumcost || !reg_cost.test (v.maximumcost) || v.maximumcost == 0) die ('maximumcost','Укажите, пожалуйста, корректное значение предельной стоимости')

        var total = v.fund + v.regionbudget + v.municipalbudget + v.owners

        if (total == 0) die ('fund', 'Укажите, пожалуйста, хотя бы один источник финансирования')

        v.house_uuid = $_REQUEST.id

        var tia = {type: 'overhaul_address_program_house_works'}
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