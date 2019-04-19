define ([], function () {

    var form_name = 'overhaul_short_program_common_form'

    $_DO.approve_overhaul_short_program_common = function (e) {
        var data = clone ($('body').data ('data'))

        if (data.item.not_all_works_approved) die ('foo', 'В краткосрочной программе капитального ремонта присутствуют неразмещенные виды работ')

        var warning = 'Разместить эти данные в ГИС ЖКХ?' + 
            (data.item.last_succesfull_status == -31 ? '\nДанная операция инициирует публикацию КПР в ГИС ЖКХ, что делает ее дальнейшее редактирование невозможным' : '')
        if (!confirm (warning)) return
        query ({type: 'overhaul_short_programs', action: 'approve'}, {}, reload_page)
    }

    $_DO.alter_overhaul_short_program_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'overhaul_short_programs', action: 'alter'}, {}, reload_page)
    }

    $_DO.cancel_overhaul_short_program_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'overhaul_short_programs'}, {}, function (data) {

            data.__read_only = true
            
            var it = data.item

            $_F5 (data)

        })

    }
    
    $_DO.edit_overhaul_short_program_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_overhaul_short_program_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var it = $('body').data ('data').item
        
        var f = w2ui [form_name]
        var v = f.values ()

        var reg_year = /^[1-9][0-9]{3}$/
        var reg_month = /^[1-9][0-2]{0,1}$/

        if (!v.programname) die ('programname', 'Укажите, пожалуйста, наименование КПР')
        
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

        if (v.endyear - v.startyear > 3) die ('endyear', 'Период реализации КПР не должен превышать 3-х лет')
                                               
        query ({type: 'overhaul_short_programs', action: 'update'}, {data: v}, reload_page)
        
    }
    
    $_DO.delete_overhaul_short_program_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'overhaul_short_programs', action: 'delete'}, {}, reload_page)
    }

    $_DO.annul_overhaul_short_program_common = function (e) {   
        if (!confirm ('Аннулировать эту запись, Вы уверены?')) return        
        query ({type: 'overhaul_short_programs', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_overhaul_short_program_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('overhaul_short_program_common.active_tab', name)
            
        use.block (name)        
    
    }

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        var it = data.item

        data.active_tab = localStorage.getItem ('overhaul_short_program_common.active_tab') || 'overhaul_short_program_common_log'

        data.__read_only = 1
                        
        done (data)
        
    }

})