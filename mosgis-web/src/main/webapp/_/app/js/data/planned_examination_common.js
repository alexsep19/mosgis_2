define ([], function () {

    var form_name = 'planned_examination_common_form'

    $_DO.open_regulator_orgs_popup = function (e) {
    
        var f = w2ui [form_name]
        
        var saved = {
            data: clone ($('body').data ('data')),
            record: clone (f.record)
        }
        
        function done () {

            $('body').data ('data', saved.data)

            w2ui[form_name].record['regulator_label'] = saved.record.regulator_label
            w2ui[form_name].record['regulator_uuid'] = saved.record.regulator_uuid
            w2ui[form_name].refresh ()

        }
    
        $('body').data ('voc_organizations_popup.callback', function (r) {

            if (!r) return done ()

            query ({type: 'voc_organizations', id: r.uuid}, {}, function (org) {

                var perms = org.vc_orgs_nsi_20.filter ((x) => {
                    return x.code == 4
                })

                if (perms.length == 0) die ('foo', 'Указанная организация не обладает полномочиями для проведения проверки')

                saved.record.regulator_uuid = r.uuid
                saved.record.regulator_label = r.label
            
                done ()
            
            })

        })

        use.block ('voc_organizations_popup')

    }

    $_DO.open_subject_orgs_popup = function (e) {
    
        var f = w2ui [form_name]
        
        var saved = {
            data: clone ($('body').data ('data')),
            record: clone (f.record)
        }
        
        function done () {

            $('body').data ('data', saved.data)

            w2ui[form_name].record['subject_label'] = saved.record.subject_label
            w2ui[form_name].record['subject_uuid'] = saved.record.subject_uuid
            w2ui[form_name].refresh ()

        }
    
        $('body').data ('voc_organizations_popup.callback', function (r) {

            if (!r) return done ()

            saved.record.subject_uuid = r.uuid
            saved.record.subject_label = r.label
            
            done ()

        })

        use.block ('voc_organizations_popup')

    }

    $_DO.cancel_planned_examination_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'planned_examinations'}, {}, function (data) {
            data.__read_only = true
            data.item.shouldberegistered = data.item['plan.shouldberegistered']
            $_F5 (data)
        })

    }
    
    $_DO.edit_planned_examination_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_planned_examination_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var data = clone ($('body').data ('data'))

        var f = w2ui [form_name]

        var v = f.values ()

        var regexp_number = /^\d{1,3}$/
        var regexp_reg_number = /^\d{14}$/
        var year_min = 1992
        var year_max = 2030
        var date_min = new Date (year_min, 1, 1)
        var date_max = new Date (year_max, 12, 31)
        var regexp_func_reg_number = /^\d{1,36}$/
        var regexp_month = /^\d{1,2}$/
        var regexp_year = /^\d{4}$/
        var regexp_double = /^(0|[1-9][0-9]*)([.][0-9][1-9]+)?$/

        if (!v.numberinplan) die ('numberinplan', 'Пожалуйста, укажите номер проверки')
        if (!regexp_number.test (v.numberinplan)) die ('numberinplan', 'Указан неверный номер проверки')

        if (data.item.shouldberegistered) {

            if (v.uriregistrationnumber == null) die ('uriregistrationnumber', 'Пожалуйста, укажите регистрационный номер')
            if (!regexp_reg_number.test (v.uriregistrationnumber)) die ('uriregistrationnumber', 'Указан неверный регистрационный номер')

            if (v.uriregistrationdate == null) die ('uriregistrationdate', 'Пожалуйста, укажите дату регистрации')
            if (Date.parse (v.uriregistrationdate) < date_min || Date.parse (v.uriregistrationdate) > date_max) die ('uriregistrationdate', 'Указана неверная дата регистрации')

        }

        if (v.functionregistrynumber && !regexp_func_reg_number.test (v.functionregistrynumber)) die ('functionregistrynumber', 'Указан неверный реестровый номер функции органа жилищного надзора')
        if (v.lastexaminationenddate && (Date.parse (v.lastexaminationenddate) < date_min || Date_parse (v.lastexaminationenddate) > date_max)) die ('lastexaminationenddate', 'Указана неверная дата окончания последней проверки')

        if (v.monthfrom) {

            if (!regexp_month.test (v.monthfrom) || v.monthfrom > 12) die ('monthfrom', 'Указан неверный месяц начала проверки')
            if (!v.yearfrom) die ('yearfrom', 'Пожалуйста, укажите год начала проверки')

        }

        if (v.yearfrom) {

            console.log (regexp_year.test (v.yearfrom))

            if (!regexp_year.test (v.yearfrom) || v.yearfrom < year_min || v.yearfrom > year_max) die ('yearfrom', 'Указан неверный год начала проверки')
            if (!v.monthfrom) die ('monthfrom', 'Пожалуйста, укажите месяц начала проверки')

        }

        if (v.workdays && !regexp_double.test (v.workdays)) die ('workdays', 'Указано неверное значение количества рабочих дней')
        if (v.workhours && !regexp_double.test (v.workhours)) die ('workhours', 'Указано неверное значение количества рабочих часов')

        query ({type: 'planned_examinations', action: 'update'}, {data: v}, reload_page)

    } 
    
    $_DO.delete_planned_examination_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'planned_examinations', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_planned_examination_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('planned_examination_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.__read_only = 1
        
        done (data)                  
        
    }

})