define ([], function () {

    var form_name = 'interval_new_form'

    $_DO.update_interval_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()

        if (!v.code_vc_nsi_3) die ('code_vc_nsi_3', 'Укажите, пожалуйста, вид коммунальной услуги')
        if (!v.code_vc_nsi_239) die('code_vc_nsi_239', 'Укажите, пожалуйста, вид коммунального ресурса')
        if (!v.startdateandtime) die('startdateandtime', 'Укажите, пожалуйста, дату и время начала перерыва')

        v [get_ref ()] = $_REQUEST.id

        query ({type: 'intervals', id: undefined, action: 'create'}, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу перерыва?').yes (function () {openTab ('/interval/' + data.id)})
            
            var grid = w2ui [$_REQUEST.type + '_intervals_grid']

            grid.reload (grid.refresh)

        })
        
    }

    function get_ref() {

        switch ($_REQUEST.type) {
            case 'mgmt_contract':
                return 'uuid_contract'
            case 'supply_resource_contract':
                return 'uuid_sr_ctr'
            case 'charter_object':
                return 'uuid_charter_object'
        }
    }

    return function (done) {

        var data = clone ($('body').data ('data'))
                                       
        data.record = $_SESSION.delete ('record') || {}

        var v = {}

        v [get_ref()] = $_REQUEST.id

        var tia = {type: 'supply_resource_contract_subjects', id: undefined}

        query(tia, {limit: 10000, offset: 0, data: v}, function (d) {

            data.service2resource = {}

            $.each(d.tb_sr_ctr_subj, function () {
                data.service2resource[this.code_vc_nsi_3] = data.service2resource[this.code_vc_nsi_3] || []
                data.service2resource[this.code_vc_nsi_3].push({
                    id: this.code_vc_nsi_239,
                    text: data.vc_nsi_239[this.code_vc_nsi_239]
                })
            })
            data.vc_nsi_3.items = data.vc_nsi_3.items.filter(function(i){
                return !!data.service2resource[i.id]
            })

            done(data)
        })
    }

})