define ([], function () {

    $_DO.update_account_ind_new = function (e) {

        var form = w2ui ['account_ind_new_form']

        var v = form.values ()
        if (v.accountnumber == null) die ('accountnumber', 'Укажите, пожалуйста, номер лицевого счёта')
        if (!/[0-9а-яА-Яa-zA-Z]/.test (v.accountnumber)) die ('accountnumber', 'Некорректный номер лицевого счёта')
        if (!v.uuid_person_customer) die ('uuid_person_customer', 'Укажите, пожалуйста, плательщика')
        if (!(v.totalsquare >= 0.01)) die ('totalsquare', 'Укажите, пожалуйста, корректный размер общей площади')

        switch (v.isaccountsdivided) {
            case 0: case 1: break
            default: v.isaccountsdivided = null
        }

        switch (v.isrenter) {
            case 0: case 1: break
            default: v.isrenter = null
        }
        
        function get_ref () {
        
            switch ($_REQUEST.type) {
                case 'mgmt_contract': return 'uuid_contract'
                case 'charter':       return 'uuid_charter'
            }
            
        }
        
        v [get_ref ()] = $_REQUEST.id
        v.id_type = 1
        
        query ({type: 'accounts', id: undefined, action: 'create'}, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу лицевого счёта?').yes (function () {openTab ('/account/' + data.id)})
            
            var grid = w2ui [$_REQUEST.type + '_accounts_grid']

            grid.reload (grid.refresh)

        })
        
    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = {
            isaccountsdivided: -1,
            isrenter: -1,
        }
                
        done (data)

    }

})