define ([], function () {

    var form_name = 'charter_object_common_form'    

    $_DO.cancel_charter_object_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'charter_objects'}, {}, function (data) {

            data.__read_only = true

            data.item.startdate = dt_dmy (data.item.startdate)
            data.item.enddate = dt_dmy (data.item.enddate)
            
            $_F5 (data)

        })

    }
    
    $_DO.edit_charter_object_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_charter_object_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()
        
        if (v.enddate) {
            if (v.enddate < v.startdate) die ('enddate', ' Дата начала управления превышает дату окончания управления')
        }
        
        v.id_reason = v.uuid_charter_file ? 2 : 1

        query ({type: 'charter_objects', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.create_house_charter_object_common = function (e) {
        use.block ('charter_object_new_house_passport');
    }    
    
    $_DO.delete_charter_object_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'charter_objects', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.undelete_charter_object_common = function (e) {   
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return        
        query ({type: 'charter_objects', action: 'undelete'}, {}, reload_page)
    }
    
    $_DO.annul_charter_object_common = function (e) {   
        use.block ('charter_object_annul_popup');
    }
    
    $_DO.choose_tab_charter_object_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('charter_object_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('charter_object_common.active_tab') || 'charter_object_common_log'

        data.__read_only = 1

        if ($_USER.role.admin) data.item.org_label = data.item ['vc_orgs.label']
        
        var it = data.item
        
        it.status_label     = data.vc_gis_status [it.id_ctr_status]
        if (it.id_ctr_status != it.id_ctr_status_gis) it.gis_status_label = data.vc_gis_status [it.id_ctr_status_gis]
        
        it.startdate = dt_dmy (it.startdate)
        it.enddate = dt_dmy (it.enddate)
        
//        done (data)


        query ({type: "charter_docs", id: undefined}, {search: [
        
            {field: "uuid_charter", operator: "is", value: data.item.uuid_charter},
            {field: "uuid_charter_object", operator: "is", value: data.item.uuid},
            
        ]}, function (d) {
        
            var a = [{id: "", text: "Текущий устав"}]
            
            $.each (d.tb_charter_files, function () {
                a.push ({id: this.id, text: "Протокол собрания " + this.label})
            })
            
            data.agreements = a
            
            done (data)

        })                      
        
    }

})