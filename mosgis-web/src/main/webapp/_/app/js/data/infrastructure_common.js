define ([], function () {

    var form_name = 'infrastructure_common_form'

    $_DO.open_orgs_infrastructure_popup = function (e) {

        var f = w2ui [form_name]
            
        function done () {
            f.refresh ()
        }

        var search_values = [{id: "2"}]
        if ($_USER.role.admin) search_values.push ({id: "8"})

        $('body').data ('voc_organizations_popup.post_data', {search:[
            {field: 'code_vc_nsi_20', operator: 'in', value: search_values}
        ], searchLogic: "AND"})
    
        $('body').data ('voc_organizations_popup.callback', function (r) {

            if (!r) return done ()

            f.record.manageroki = r.uuid
            f.record.manageroki_label = r.label

            done ()

        })

        use.block ('voc_organizations_popup')

    }

    $_DO.open_oktmo_popup = function (e) {

        var f = w2ui [form_name]
            
        function done () {
            f.refresh ()
        }

        $_SESSION.set ('voc_oktmo_popup.ids', [])

        var oktmos = Object.keys ($_USER.role).filter ((x) => x.startsWith ('oktmo_')).map ((x) => {
            return x.substring ('oktmo_'.length)
        })

        $('body').data ('voc_oktmo_popup.callback', function (r) {

            if (!r) return done ()

            if ($_USER.role.nsi_20_8 && !(r.code in oktmos)) alert ('Недопустимый код ОКТМО')
            else {
                f.record.oktmo = r.recid
                f.record.oktmo_code = r.code
            }

            done ()

        })

        use.block ('voc_oktmo_popup')

    }

    $_DO.cancel_infrastructure_common = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'infrastructures'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_infrastructure_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_infrastructure_common = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return
        
        var f = w2ui [form_name]

        var v = f.values ()

        var reg_year = /^[1-2][0-9]{3}$/

        console.log (v)

        var elements_nsi_33 = {
            independentsource: {'1.1':1, '2.1':1, '5.1':1, '5.2':1},
            code_vc_nsi_34:    {'1.1':1},
            code_vc_nsi_35:    {'4.5':1},
            code_vc_nsi_40:    {'2.1':1},
            code_vc_nsi_37:    {'5.2':1},
            code_vc_nsi_38:    {'5.1':1}
        }

        if (!v.name) die ('name', 'Укажите, пожалуйста, наименование объекта')
        if (!v.code_vc_nsi_39) die ('code_vc_nsi_39', 'Укажите, пожалуйста, основание управления')
        if (!v.hasOwnProperty('indefinitemanagement')) die ('indefinitemanagement', 'Укажите, пожалуйста, признак бессрочности управления')
        if (!v.indefinitemanagement && !v.endmanagmentdate) die ('endmanagmentdate', 'Укажите, пожалуйста, дату окончания управления')
        if (!v.code_vc_nsi_33) die ('code_vc_nsi_33', 'Укажите, пожалуйста, вид объекта')
        if (!v.oktmo) die ('oktmo_code', 'Укажите, пожалуйста, код ОКТМО')
        if (!v.comissioningyear) die ('comissioningyear', 'Укажите, пожалуйста, год ввода в эксплуатацию')
        if (!reg_year.test (v.comissioningyear) || v.comissioningyear < 1600) die ('comissioningyear', 'Указано неверное значение года ввода в эксплуатацию') 

        Object.keys(elements_nsi_33).forEach ((value, index, array) => {
            if (elements_nsi_33[value][v.code_vc_nsi_33] && !v[value]) die (value, 'Указаны не все необходимые данные')
        })

        v.code_vc_nsi_3 = w2ui ['code_vc_nsi_3_grid'].getSelection ()
        if (!v.code_vc_nsi_3.length) die ('foo', 'Укажите, пожалуйста, по крайней мере один вид коммунальных услуг')
        
        delete v.manageroki_label
        delete v.oktmo_code

        query ({type: 'infrastructures', action: 'update'}, {data: v}, reload_page)

    }
    
    $_DO.delete_infrastructure_common = function (e) {   
        if (!confirm ('Удалить эту запись, Вы уверены?')) return        
        query ({type: 'infrastructures', action: 'delete'}, {}, reload_page)
    }
    
    $_DO.choose_tab_infrastructure_common = function (e) {
    
        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('infrastructure_common.active_tab', name)
            
        use.block (name)        
    
    }
    
    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('infrastructure_common.active_tab') || 'infrastructure_common_log'

        data.__read_only = 1

        function perms () {

            if ((data.item.id_is_status != 10 && data.item.id_is_status != 11) || data.item.is_deleted)
                return false;

            if ($_USER.role.admin) return true

            if ($_USER.role.nsi_20_8) {

                var oktmos = Object.keys ($_USER.role).filter ((x) => x.startsWith ('oktmo_')).map ((x) => {
                    return x.substring ('oktmo_'.length)
                })

                if (data.item.manageroki == $_USER.uuid_org) {

                    if (!data.item.oktmo || data.item.oktmo_code in oktmos) return true

                }

            }

            if ($_USER.role.nsi_20_2) {

                if (data.item.manageroki == $_USER.uuid_org) return true

            }

            return false

        }

        var mod_perms = perms ()

        data.item._can = {
            edit: mod_perms,
            update: mod_perms,
            cancel: mod_perms,
            delete: mod_perms,
        }

        done (data)
        
    }

})